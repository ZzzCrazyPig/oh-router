package com.crazypig.oh.proxy.config;

import com.crazypig.oh.proxy.ProxyServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by chenjianxin on 2021/7/7.
 */
@Configuration
public class ProxyServerConfiguration {

    /**
     * proxy server的监听端口
     */
    @Value("${proxy.port:9000}")
    private int port;

    @Bean
    ProxyServer proxyServer() {
        ProxyServer proxyServer = new ProxyServer();
        proxyServer.setPort(port);
        return proxyServer;
    }

}
