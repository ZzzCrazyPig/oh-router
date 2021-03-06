package com.crazypig.oh.router.internal;

import com.crazypig.oh.common.protocol.Address;
import com.crazypig.oh.common.protocol.RouteInfo;
import com.crazypig.oh.common.util.HostUtils;
import com.crazypig.oh.router.exception.BindException;
import com.crazypig.oh.router.exception.RouteException;
import com.crazypig.oh.router.RouteServer;
import com.crazypig.oh.router.model.ServerBindStatView;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.SocketUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Created by chenjianxin on 2021/7/4.
 */
@Slf4j
public class NettyRouteService implements RouteService, InitializingBean, DisposableBean {

    private static final int MIN_PORT = 20000;

    private static Supplier<ServerBootstrap> serverProvider = ServerBootstrap::new;

    private static Supplier<Bootstrap> clientProvider = Bootstrap::new;

    private Map<RouteInfo, ServerBinder> serverBinderMap = new ConcurrentHashMap<>();

    private EventLoopGroup acceptorGroup;

    private EventLoopGroup ioWorkerGroup;

    @Setter
    private int acceptorSize = 1;

    @Setter
    private int ioWorkerSize = Runtime.getRuntime().availableProcessors();

    /**
     * proxy server host
     */
    @Setter
    private String proxyHost;

    /**
     * proxy server port
     */
    @Setter
    private int proxyPort;


    @Override
    public void afterPropertiesSet() throws Exception {

        this.acceptorGroup = new NioEventLoopGroup(acceptorSize, new
                DefaultThreadFactory("AcceptorGroup"));

        this.ioWorkerGroup = new NioEventLoopGroup(ioWorkerSize,
                new DefaultThreadFactory("IOWorkerGroup"));

    }

    @Override
    public void destroy() throws Exception {

        shutdown(acceptorGroup);
        shutdown(ioWorkerGroup);

    }

    @Override
    public Future<RouteSession> route(Channel frontendChannel, RouteInfo routeInfo) throws Exception {

        Promise<RouteSession> routePromise = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);

        Bootstrap client = clientProvider.get();
        EventLoopGroup clientGroup = frontendChannel instanceof EmbeddedChannel ? ioWorkerGroup : frontendChannel.eventLoop();
        client.group(clientGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(RouteBackendChannelInitializer.INSTANCE);

        // connect proxy server
        ChannelFuture channelFuture = client.connect(proxyHost, proxyPort);

        channelFuture.addListener((ChannelFutureListener) f -> {

            Channel backendChannel = f.channel();
            RouteSession session = new RouteSession(routeInfo, routePromise, frontendChannel, backendChannel);
            frontendChannel.attr(RouteServer.SESSION).set(session);
            backendChannel.attr(RouteServer.SESSION).set(session);

            if (!f.isSuccess()) {
                session.onRouteFail(f.cause());
                return;
            }

            log.info("Connect ProxyServer {}:{} successfully, try routing to {}", proxyHost, proxyPort, routeInfo);

        });

        return routePromise;

    }

    @Override
    public Address bind(RouteInfo routeInfo, long timeoutInSeconds) throws Exception {

        Channel frontendChannel = new EmbeddedChannel();

        Future<RouteSession> routePromise = route(frontendChannel, routeInfo);

        RouteSession session = null;

        try {
            session = routePromise.get(timeoutInSeconds, TimeUnit.SECONDS);
            log.info("Routing {} successfully", routeInfo);
            // route successfully, bind server port
            return this.doBind(routeInfo);
        }
        catch (ExecutionException e) {
            releaseChannel(frontendChannel);
            throw new RouteException(e.getCause());
        }
        catch (TimeoutException e) {
            releaseChannel(frontendChannel);
            throw new RouteException("Can not route to " + routeInfo + " due to timeout");
        }
        finally {
            if (session != null) {
                log.info("RouteForBinding, close session : [{}]", session.id());
                session.close();
            }
        }

    }

    @Override
    public List<ServerBindStatView> queryServerBindStat() {
        List<ServerBindStatView> resultList = new ArrayList<>();
        this.serverBinderMap.forEach((k, v) -> {
            resultList.add(new ServerBindStatView(k, v.getBindAddress(), v.size()));
        });
        return resultList;
    }

    private Address doBind(RouteInfo routeInfo) throws Exception {

        Address bindAddress = null;

        ServerBinder serverBinder = serverBinderMap.get(routeInfo);
        if (serverBinder != null && serverBinder.available()) {
            // reuse bind address when exists
            bindAddress = serverBinder.getBindAddress();
            log.info("Already exist doBind address {} for routing {}", bindAddress, routeInfo);
            return bindAddress;
        }

        int bindPort = nextIdlePort();
        bindAddress = new Address(HostUtils.serverHost(), bindPort);
        serverBinder = new ServerBinder(bindAddress);

        ServerBootstrap serverBootstrap = serverProvider.get();
        ChannelFuture channelFuture = serverBootstrap
                .group(acceptorGroup, ioWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 200)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new RouteFrontendChannelInitializer(routeInfo, serverBinder))
                .bind(bindAddress.getHost(), bindAddress.getPort())
                .sync();

        if (!channelFuture.isSuccess()) {
            throw new BindException(channelFuture.cause());
        }

        serverBinder.ready(channelFuture.channel(), new ServerBinderListener() {
            @Override
            public void onClosed(ServerBinder binder) {
                boolean removed = serverBinderMap.remove(routeInfo, binder);
                if (removed) {
                    log.info("Remove serverBinder of : " + routeInfo);
                }
            }
        });
        log.info("BindRouteServer using port {} for routing {}", bindPort, routeInfo);
        ServerBinder exitsServerBinder = serverBinderMap.putIfAbsent(routeInfo, serverBinder);
        if (exitsServerBinder != null) {
            if (exitsServerBinder.available()) {
                // exits and available, using old doBind
                bindAddress = exitsServerBinder.getBindAddress();
                // new doBind release
                channelFuture.channel().close();
            }
            else {
                // exits but unavailable, using new doBind
                serverBinderMap.put(routeInfo, serverBinder);
                bindAddress = serverBinder.getBindAddress();
            }
        }
        else {
            bindAddress = serverBinder.getBindAddress();
        }

        return bindAddress;
    }

    private int nextIdlePort() throws InterruptedException {
        return SocketUtils.findAvailableTcpPort(MIN_PORT);
    }

    private void releaseChannel(Channel channel) {
        RouteSession session = channel.attr(RouteServer.SESSION).get();
        if (session != null) {
            session.close();
        }
        else {
            channel.close();
        }
    }

    private void shutdown(EventLoopGroup group) {
        if (group != null) {
            group.shutdownGracefully();
        }
    }

}
