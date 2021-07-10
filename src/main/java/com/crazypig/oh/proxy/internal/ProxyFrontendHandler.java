package com.crazypig.oh.proxy.internal;

import com.alibaba.fastjson.JSON;
import com.crazypig.oh.common.protocol.Address;
import com.crazypig.oh.common.protocol.Command;
import com.crazypig.oh.common.protocol.ConnectCommand;
import com.crazypig.oh.common.protocol.ConnectResponse;
import com.crazypig.oh.proxy.ProxyServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by chenjianxin on 2021/6/26.
 * 代理服务前端链路的处理
 */
@Slf4j
public class ProxyFrontendHandler extends ChannelInboundHandlerAdapter {

    private static final String LOG_PREFIX = "Proxy-Frontend";

    private ProxyServer proxyServer;

    public ProxyFrontendHandler(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info(LOG_PREFIX + " channel [{}] active", ctx.channel().id());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info(LOG_PREFIX + " channel [{}] inactive", ctx.channel().id());
        ProxySession session = sessionOf(ctx.channel(), false);
        if (session != null) {
            session.close();
        } else {
            closeChannel(ctx.channel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        // 已连接上, 读到的数据原样发送到后端channel
        ProxySession session = sessionOf(ctx.channel(), false);
        if (session != null && session.establish()) {
            session.handleRead(ctx, msg);
            return;
        }

        String message = (String) msg;
        log.info(LOG_PREFIX + " channel [{}] receive request : {}", ctx.channel().id(), message);
        Command command = Command.parse(message);
        switch (command.getType()) {
            case CONNECT:
                // 连接目标
                handleConnect(ctx, message);
                break;
            case ACK:
                session.onEstablished();
                break;
            default:
                log.error(LOG_PREFIX + " Unknown command type : {}", command.getType());
                // 非法命令, 关闭链路
                closeChannel(ctx.channel());
                break;
        }

    }

    protected void handleConnect(ChannelHandlerContext ctx, String message) {

        ConnectCommand connectCommand = ConnectCommand.parse(message);
        Address target = connectCommand.target();

        ChannelFuture connectFuture = proxyServer.connect(
                ctx.channel(),
                target.getHost(),
                target.getPort());

        connectFuture.addListener(future -> {

            if (!future.isSuccess()) {
                // 连接失败的处理
                ConnectResponse failResponse = new ConnectResponse();
                failResponse.fail(future.cause().getMessage());
                failResponse.resp(ctx.channel());
                return;
            }

            // 链接目标端成功, 创建会话
            Channel backendChannel = connectFuture.channel();
            Channel frontendChannel = ctx.channel();

            ProxySession session = new ProxySession(connectCommand.getSessionId(), frontendChannel, backendChannel);
            backendChannel.attr(ProxyServer.SESSION_KEY).set(session);
            frontendChannel.attr(ProxyServer.SESSION_KEY).set(session);

            // 链接建立成功, 写响应消息
            ConnectResponse connectResponse = new ConnectResponse();
            connectResponse.success(session.id());
            ChannelFuture respFuture = connectResponse.resp(frontendChannel);

            respFuture.addListener(f -> {

                if (!f.isSuccess()) {
                    log.error("Error on session resp, session : " + session.id(), f.cause());
                }

            });

        });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(LOG_PREFIX + " channel [" + ctx.channel().id() + "] error", cause);
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
