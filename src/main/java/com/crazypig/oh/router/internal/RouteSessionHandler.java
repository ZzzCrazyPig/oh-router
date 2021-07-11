package com.crazypig.oh.router.internal;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by chenjianxin on 2021/7/4.
 */
public interface RouteSessionHandler {

    ChannelFuture handleRoute();

    void handleRead(ChannelHandlerContext ctx, Object msg);

    void onRouteFail(Throwable cause);

    void onEstablished();

}
