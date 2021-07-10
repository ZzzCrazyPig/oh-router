package com.crazypig.oh.router.internal;

import com.crazypig.oh.common.protocol.Address;
import com.crazypig.oh.common.protocol.RouteInfo;
import com.crazypig.oh.router.model.ServerBindStatView;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;

import java.util.List;

/**
 * Created by chenjianxin on 2021/7/4.
 */
public interface RouteService {

    Future<RouteSession> route(Channel frontendChannel, RouteInfo routeInfo) throws Exception;

    Address bind(RouteInfo routeInfo, long timeoutInSeconds) throws Exception;

    List<ServerBindStatView> queryServerBindStat();

}
