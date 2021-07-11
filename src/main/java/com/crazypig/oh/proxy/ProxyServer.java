package com.crazypig.oh.proxy;

import com.crazypig.oh.common.util.HostUtils;
import com.crazypig.oh.proxy.internal.ProxyBackendChannelInitializer;
import com.crazypig.oh.proxy.internal.ProxyFrontendChannelInitializer;
import com.crazypig.oh.proxy.internal.ProxySession;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.util.function.Supplier;


/**
 * Created by chenjianxin on 2021/6/26.
 *
 * ProxyServer is responsible for dealing with the real target connection
 * and needs to be deployed in a network domain that can connect to the target
 */
@Slf4j
public class ProxyServer implements InitializingBean {

    public final static AttributeKey<ProxySession> SESSION_KEY = AttributeKey.valueOf("session");

    private ServerBootstrap server = new ServerBootstrap();

    private Supplier<Bootstrap> clientProvider = Bootstrap::new;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    /**
     * proxy server listening port
     */
    @Setter
    private int port;

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void start() throws Exception {

        // init proxy server
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("ProxyAcceptor"));
        workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(), new DefaultThreadFactory("ProxyIOWorker"));
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ProxyFrontendChannelInitializer(this)).bind(port).sync();

        log.info("ProxyServer started at {} and bind on {}", HostUtils.serverHost(), this.port);
    }

    public ChannelFuture connect(Channel frontendChannel, String host, int port) {
        Bootstrap client = clientProvider.get();
        client.group(frontendChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ProxyBackendChannelInitializer());
        return client.connect(host, port);
    }

}
