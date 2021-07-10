package com.crazypig.oh.proxy.internal;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by chenjianxin on 2021/7/4.
 */
public interface ProxySessionHandler {

    void handleRead(ChannelHandlerContext ctx, Object msg);

    void onEstablished();

}
