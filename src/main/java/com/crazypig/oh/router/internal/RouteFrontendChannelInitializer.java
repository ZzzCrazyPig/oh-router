package com.crazypig.oh.router.internal;

import com.crazypig.oh.common.protocol.RouteInfo;
import com.crazypig.oh.router.RouteServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;

/**
 * Created by chenjianxin on 2021/7/4.
 */
public class RouteFrontendChannelInitializer extends ChannelInitializer {

    private RouteInfo routeInfo;

    private ChannelGroup channelGroup;

    public RouteFrontendChannelInitializer(RouteInfo routeInfo, ChannelGroup channelGroup) {
        this.routeInfo = routeInfo;
        this.channelGroup = channelGroup;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline().addLast("routeFrontendHandler", new RouteFrontendHandler(routeInfo, channelGroup, RouteServer.INSTANCE.get().getRouteService()));
    }
}
