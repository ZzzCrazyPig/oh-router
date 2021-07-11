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
        ProxyFrontendChannelInitializer.unCodec(frontendChannel);
        this.state = ProxySessionState.ESTABLISHED;
        Channel backendChannel = backend();
        // enable backend channel to read
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
        Channel frontendChannel = frontend();
        frontendChannel.writeAndFlush(msg);
    }

    @Override
    public boolean established() {
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
