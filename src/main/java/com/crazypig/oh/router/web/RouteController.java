package com.crazypig.oh.router.web;

import com.crazypig.oh.common.protocol.Address;
import com.crazypig.oh.common.protocol.RouteInfo;
import com.crazypig.oh.common.web.BaseResult;
import com.crazypig.oh.router.RouteServer;
import com.crazypig.oh.router.internal.RouteService;
import com.crazypig.oh.router.model.ServerBindStatView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by chenjianxin on 2021/6/27.
 */
@Slf4j
@RestController
@RequestMapping("/route")
public class RouteController {

    @Autowired
    private RouteServer routeServer;

    @RequestMapping("/bind/{host}/{port}")
    public BaseResult<RouteInfo> route(@PathVariable String host,
                                       @PathVariable int port) throws Exception {
        RouteService routeService = routeServer.getRouteService();
        Address bindAddress = routeService.bind(new RouteInfo(host, port), 5);
        return BaseResult.ok(bindAddress);
    }

    @RequestMapping("/queryServerBindStat")
    public BaseResult<List<ServerBindStatView>> queryServerBindStat() {
        return BaseResult.ok(routeServer.getRouteService().queryServerBindStat());
    }

}
