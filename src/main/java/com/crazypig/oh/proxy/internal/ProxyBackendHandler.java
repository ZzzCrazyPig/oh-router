package com.crazypig.oh.proxy.internal;

import com.crazypig.oh.proxy.ProxyServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Created by chenjianxin on 2021/6/26.
 *
 * 负责真正与目标端通讯的处理器
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
            closeChannel(ctx.channel());
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
        // 遇到异常, 关闭链路
        ProxySession session = sessionOf(ctx.channel(), false);
        if (session != null) {
            session.close();
        }
        else {
            closeChannel(ctx.channel());
        }
    }

    private ProxySession sessionOf(Channel channel, boolean assertExists) {
        ProxySession session = channel.attr(ProxyServer.SESSION_KEY).get();
        if (assertExists && session == null) {
            throw new IllegalStateException("Session leak of channel [" + channel.id() + "]");
        }
        return session;
    }

    private void closeChannel(Channel channel) {
        if (channel == null) {
            return;
        }
        if (channel.isOpen()) {
            channel.close();
        }
    }

}
