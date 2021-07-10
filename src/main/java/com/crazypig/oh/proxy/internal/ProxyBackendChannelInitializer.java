package com.crazypig.oh.proxy.internal;


import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

/**
 * Created by chenjianxin on 2021/7/4.
 */
public class ProxyBackendChannelInitializer extends ChannelInitializer {

    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline().addLast("proxyBackendHandler", new ProxyBackendHandler());
    }
}
