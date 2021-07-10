package com.crazypig.oh.main;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.Optional;

/**
 * Created by chenjianxin on 2020/5/23.
 */
public class WebBootstrap extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        String serverMode = Optional.ofNullable(System.getProperty("server.mode")).orElse("router");
        if ("proxy".equals(serverMode)) {
            return builder.sources(ProxyBootstrap.class);
        }
        if ("all".equals(serverMode)) {
            return builder.sources(ProxyBootstrap.class, RouterBootstrap.class);
        }
        return builder.sources(RouterBootstrap.class);
    }

}
