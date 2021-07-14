package com.crazypig.oh.router.internal;

import com.crazypig.oh.common.protocol.RouteInfo;
import com.crazypig.oh.common.util.ChannelUtils;
import com.crazypig.oh.router.exception.RouteException;
import com.crazypig.oh.router.RouteServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by chenjianxin on 2021/6/27.
 */
@Slf4j
public class RouteFrontendHandler extends ChannelInboundHandlerAdapter {

    private static final String LOG_PREFIX = "Route-Frontend";

    private RouteInfo routeInfo;

    private ChannelGroup channelGroup;

    private RouteService routeService;

    public RouteFrontendHandler(RouteInfo routeInfo,
                                ChannelGroup channelGroup,
                                RouteService routeService) {
        this.routeInfo = routeInfo;
        this.channelGroup = channelGroup;
        this.routeService = routeService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        Channel frontendChannel = ctx.channel();

        log.info(LOG_PREFIX + " channel [{}] active, try to route {} using proxy", frontendChannel.id(), routeInfo);

        channelGroup.add(frontendChannel);

        // try to route new channel using route server ...
        // disable auto read, avoid client send message when route channel is not yet established
        frontendChannel.config().setAutoRead(false);

        Future<RouteSession> routeFuture = routeService.route(frontendChannel, routeInfo);

        routeFuture.addListener(f -> {

            if (!f.isSuccess()) {
                log.error("Can not route to " + routeInfo + " of channel [" + frontendChannel.id() + "]", f.cause());
                ChannelUtils.close(frontendChannel);
                return;
            }

            RouteSession session = (RouteSession) f.get();
            Channel backendChannel = session.backend();
            log.info("{} route successfully : [{}] -> [{}], session [{}]", routeInfo,
                    frontendChannel.id(),
                    backendChannel.id(),
                    session.id());
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel frontendChannel = ctx.channel();
        log.info(LOG_PREFIX + " channel [{}] inactive", frontendChannel.id());
        RouteSession session = sessionOf(frontendChannel, false);
        if (session != null) {
            session.close();
        }
        else {
            ChannelUtils.close(frontendChannel);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel frontendChannel = ctx.channel();
        RouteSession session = sessionOf(frontendChannel, true);
        session.handleRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel frontendChannel = ctx.channel();
        log.error(LOG_PREFIX  + " channel [" + frontendChannel.id() + "] error", cause);
        RouteSession session = sessionOf(frontendChannel, false);
        if (session != null) {
            session.close();
        }
        else {
            ChannelUtils.close(frontendChannel);
        }
    }

    private RouteSession sessionOf(Channel frontendChannel, boolean assertExists) {
        RouteSession session = frontendChannel.attr(RouteServer.SESSION).get();
        if (assertExists && session == null) {
            throw new RouteException("Session leak of channel [" + frontendChannel.id() + "]");
        }
        return session;
    }

}
