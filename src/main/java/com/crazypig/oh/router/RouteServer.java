package com.crazypig.oh.router;

import com.crazypig.oh.common.util.HostUtils;
import com.crazypig.oh.router.internal.RouteService;
import com.crazypig.oh.router.internal.RouteSession;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.util.function.Supplier;

/**
 * Created by chenjianxin on 2021/6/26.
 */
@Slf4j
public class RouteServer implements InitializingBean {

    public static Supplier<RouteServer> INSTANCE;

    public static final AttributeKey<RouteSession> SESSION = AttributeKey.valueOf("session");

    @Setter
    @Getter
    private RouteService routeService;

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
        INSTANCE = () -> this;
    }

    public void start() throws Exception {
        log.info("RouteServer started at : " + HostUtils.serverHost());
    }



}
