package com.crazypig.oh.proxy.internal;

import com.crazypig.oh.common.session.ChannelSession;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Created by chenjianxin on 2021/6/29.
 */
@Slf4j
public class ProxySession extends ChannelSession implements ProxySessionHandler {

    private volatile ProxySessionState state = ProxySessionState.INIT;

    private String routeSessionId;

    public ProxySession(String routeSessionId, Channel frontendChannel, Channel backendChannel) {
        super(frontendChannel, backendChannel);
        this.routeSessionId = routeSessionId;
    }

    @Override
    public void onEstablished() {

        Channel frontendChannel = frontend();

        // 移除编解码的处理
        ProxyFrontendChannelInitializer.unCodec(frontendChannel);
        // 代理数据传输处理, 前端channel读到的数据直接write到后端channel
        this.state = ProxySessionState.ESTABLISHED;

        Channel backendChannel = backend();

        backendChannel.config().setAutoRead(true);
    }

    @Override
    public void handleRead(ChannelHandlerContext ctx, Object msg) {

        Channel channel = ctx.channel();

        if (channel == frontend()) {
            handleFrontendRead(msg);
            return;
        }

        if (channel == backend()) {
            handleBackendRead(msg);
            return;
        }

        throw new IllegalStateException("Unknown channel [" + ctx.channel().id() + "] of proxy session " + id());

    }

    private void handleFrontendRead(Object msg) {
        log.debug("session [{}] frontend channel [{}] receive data : \n {}", id(), frontend().id(),
                ByteBufUtil.prettyHexDump((ByteBuf) msg));
        Channel backendChannel = backend();
        backendChannel.writeAndFlush(msg);
    }

    private void handleBackendRead(Object msg) {
        log.debug("session [{}] backend channel [{}] receive data : \n {}", id(), backend().id(),
                ByteBufUtil.prettyHexDump((ByteBuf) msg));
        // 读到什么, 就往前端channel发送
        Channel frontendChannel = frontend();
        frontendChannel.writeAndFlush(msg);
    }

    @Override
    public boolean establish() {
        return state == ProxySessionState.ESTABLISHED;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(" " + this.state);
        sb.append(" => ").append(Optional.ofNullable(this.routeSessionId).orElse("NULL"));
        return sb.toString();
    }

}
