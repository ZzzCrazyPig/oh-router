package com.crazypig.oh.router.internal;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by chenjianxin on 2021/7/4.
 */
public interface RouteSessionHandler {

    /**
     * 主动发起route请求
     * @return
     */
    ChannelFuture handleRoute();

    /**
     * 接收io数据的响应(前后端都在此响应处理)
     * @param ctx
     * @param msg
     */
    void handleRead(ChannelHandlerContext ctx, Object msg);

    // ----- 回调处理 ------

    /**
     * route请求失败的回调
     * @param cause
     */
    void onRouteFail(Throwable cause);

    /**
     * session建立成功的回调
     */
    void onEstablished();

}
