package com.crazypig.oh.router.internal;

import com.crazypig.oh.common.session.Session;
import com.crazypig.oh.common.util.ChannelUtils;
import com.crazypig.oh.router.exception.RouteException;
import com.crazypig.oh.router.RouteServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by chenjianxin on 2021/6/26.
 */
@Slf4j
public class RouteBackendHandler extends ChannelInboundHandlerAdapter {

    private static final String LOG_PREFIX = "Route-Backend";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel backendChannel = ctx.channel();
        log.info(LOG_PREFIX + " channel [{}] active", backendChannel.id());
        RouteSession session = sessionOf(backendChannel, true);
        session.handleRoute();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel backendChannel = ctx.channel();
        log.info(LOG_PREFIX + " channel [{}] inactive", backendChannel.id());
        RouteSession session = sessionOf(backendChannel, false);
        if (session != null) {
            session.close();
        }
        else {
            ChannelUtils.close(backendChannel);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel backendChannel = ctx.channel();
        RouteSession session = sessionOf(backendChannel, true);
        session.handleRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(LOG_PREFIX + " channel [" + ctx.channel().id() + "] error", cause);
        Channel backendChannel = ctx.channel();
        Session session = sessionOf(backendChannel, false);
        if (session != null) {
            session.close();
        }
        else {
            ChannelUtils.close(backendChannel);
        }
    }

    private RouteSession sessionOf(Channel backendChannel, boolean assertExists) {
        RouteSession session = backendChannel.attr(RouteServer.SESSION).get();
        if (assertExists && session == null) {
            throw new RouteException("Session leak of channel [" + backendChannel.id() + "]");
        }
        return session;
    }

}
