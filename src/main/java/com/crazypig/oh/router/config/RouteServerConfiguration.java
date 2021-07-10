package com.crazypig.oh.router.config;

import com.crazypig.oh.router.RouteServer;
import com.crazypig.oh.router.internal.RouteService;
import com.crazypig.oh.router.internal.NettyRouteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by chenjianxin on 2021/7/7.
 */
@Configuration
public class RouteServerConfiguration {

    @Value("${proxy.host:localhost}")
    private String proxyHost;

    @Value("${proxy.port:9000}")
    private int proxyPort;

    @Bean
    RouteServer routeServer() {
        RouteServer routeServer = new RouteServer();
        routeServer.setRouteService(routeService());
        return routeServer;
    }

    @Bean
    RouteService routeService() {
        NettyRouteService routeService = new NettyRouteService();
        routeService.setProxyHost(proxyHost);
        routeService.setProxyPort(proxyPort);
        return routeService;
    }

}
