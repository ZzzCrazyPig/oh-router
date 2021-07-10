package com.crazypig.oh.common.session;

import io.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by chenjianxin on 2021/6/29.
 */
public abstract class ChannelSession implements Session<Channel> {

    private Channel frontendChannel;

    private Channel backendChannel;

    private String id;

    private AtomicBoolean closed;

    public ChannelSession(Channel frontendChannel, Channel backendChannel) {
        this.frontendChannel = frontendChannel;
        this.backendChannel = backendChannel;
        this.id = SessionIdGenerator.generate();
        this.closed = new AtomicBoolean(false);
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Channel frontend() {
        return frontendChannel;
    }

    @Override
    public Channel backend() {
        return backendChannel;
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        closeChannel(this.backendChannel);
        closeChannel(this.frontendChannel);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id)
                .append(" ( ")
                .append(this.frontend().id())
                .append(" => ")
                .append(this.backend().id())
                .append(" )");
        return builder.toString();
    }

    protected void closeChannel(Channel channel) {
        if (channel == null) {
            return;
        }
        if (channel.isOpen()) {
            channel.close();
        }
    }

}
