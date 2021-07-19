package com.crazypig.oh.router.internal;

import com.crazypig.oh.common.protocol.Address;
import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by chenjianxin on 2021/7/8.
 */
@Slf4j
public class ServerBinder extends DefaultChannelGroup {

    @Getter
    private Address bindAddress;

    private Channel serverChannel;

    private ScheduledFuture idleFuture;

    private AtomicBoolean idle = new AtomicBoolean(false);

    private AtomicBoolean closed = new AtomicBoolean(false);

    private ServerBinderListener listener = binder -> {};

    public ServerBinder(Address bindAddress) {
        super(bindAddress.toString(), GlobalEventExecutor.INSTANCE);
        this.bindAddress = bindAddress;
    }

    public void ready(Channel serverChannel, ServerBinderListener listener) {
        this.serverChannel = serverChannel;
        this.listener = listener;
        this.idleFuture = this.serverChannel.eventLoop().scheduleAtFixedRate(
                new IdleChecker(), 15, 30, TimeUnit.SECONDS
        );
    }

    @Override
    public boolean add(Channel channel) {
        idle.set(false);
        return super.add(channel);
    }

    public boolean available() {
        return !closed.get();
    }

    private void autoCloseIdle() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        // check idle again
        if (!idle.get()) {
            closed.set(false);
            return;
        }
        try {
            this.serverChannel.close();
            log.info("AutoClose idle server binder of bindAddress : " + bindAddress);
        }
        catch (Exception e) {
            log.error("Error on close server channel of bindAddress : " + bindAddress, e);
        }
        finally {
            try {
                idleFuture.cancel(true);
            }
            catch (Exception e) {
                log.error("Error on cancel idle checker of bindAddress : " + bindAddress, e);
            }
            listener.onClosed(this);
        }
    }

    private class IdleChecker implements Runnable {

        @Override
        public void run() {
            if (!isEmpty()) {
                return;
            }
            idle.set(true);
            if (idle.get()) {
                autoCloseIdle();
            }
        }
    }

}
