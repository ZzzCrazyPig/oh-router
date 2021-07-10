package com.crazypig.oh.router.model;

import com.crazypig.oh.common.protocol.Address;
import com.crazypig.oh.common.protocol.RouteInfo;
import lombok.Data;

/**
 * Created by chenjianxin on 2021/7/2.
 */
@Data
public class ServerBindStatView {

    private RouteInfo routeInfo;

    private Address bindAddress;

    private int activeChannelCount;

    public ServerBindStatView(RouteInfo routeInfo,
                              Address bindAddress,
                              int activeChannelCount) {
        this.routeInfo = routeInfo;
        this.bindAddress = bindAddress;
        this.activeChannelCount = activeChannelCount;
    }

}
