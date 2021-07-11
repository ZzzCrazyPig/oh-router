package com.crazypig.oh.proxy.internal;

import com.crazypig.oh.common.protocol.Address;
import com.crazypig.oh.common.protocol.Command;
import com.crazypig.oh.common.protocol.ConnectCommand;
import com.crazypig.oh.common.protocol.ConnectResponse;
import com.crazypig.oh.proxy.ProxyServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by chenjianxin on 2021/6/26.
 *
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

        ProxySession session = sessionOf(ctx.channel(), false);
        if (session != null && session.established()) {
            session.handleRead(ctx, msg);
            return;
        }

        String message = (String) msg;
        log.info(LOG_PREFIX + " channel [{}] receive request : {}", ctx.channel().id(), message);
        Command command = Command.parse(message);
        switch (command.getType()) {
            case CONNECT:
                handleConnect(ctx, message);
                break;
            case ACK:
                session.onEstablished();
                break;
            default:
                log.error(LOG_PREFIX + " Unknown command type : {}", command.getType());
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
                // fail to connect proxy
                ConnectResponse failResponse = new ConnectResponse();
                failResponse.fail(future.cause().getMessage());
                failResponse.resp(ctx.channel());
                return;
            }

            // connect successfully, create proxy session
            Channel backendChannel = connectFuture.channel();
            Channel frontendChannel = ctx.channel();

            ProxySession session = new ProxySession(connectCommand.getSessionId(), frontendChannel, backendChannel);
            backendChannel.attr(ProxyServer.SESSION_KEY).set(session);
            frontendChannel.attr(ProxyServer.SESSION_KEY).set(session);

            // response to RouteServer
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
