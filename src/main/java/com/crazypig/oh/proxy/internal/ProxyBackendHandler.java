package com.crazypig.oh.proxy.internal;

import com.crazypig.oh.common.util.ChannelUtils;
import com.crazypig.oh.proxy.ProxyServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by chenjianxin on 2021/6/26.
 *
 */
@Slf4j
public class ProxyBackendHandler extends ChannelInboundHandlerAdapter {

    private static final String LOG_PREFIX = "Proxy-Backend";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info(LOG_PREFIX + " channel [{}] active", ctx.channel().id());
        ctx.channel().config().setAutoRead(false);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info(LOG_PREFIX + " channel [{}] inactive", ctx.channel().id());
        ProxySession session = sessionOf(ctx.channel(), false);
        if (session != null) {
            session.close();
        }
        else {
            ChannelUtils.close(ctx.channel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ProxySession session = sessionOf(ctx.channel(), true);
        session.handleRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(LOG_PREFIX + " channel [" + ctx.channel().id() + "] error", cause);
        ProxySession session = sessionOf(ctx.channel(), false);
        if (session != null) {
            session.close();
        }
        else {
            ChannelUtils.close(ctx.channel());
        }
    }

    private ProxySession sessionOf(Channel channel, boolean assertExists) {
        ProxySession session = channel.attr(ProxyServer.SESSION_KEY).get();
        if (assertExists && session == null) {
            throw new IllegalStateException("Session leak of channel [" + channel.id() + "]");
        }
        return session;
    }

}
