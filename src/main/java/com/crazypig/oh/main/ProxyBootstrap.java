package com.crazypig.oh.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by chenjianxin on 2020/5/22.
 */
@SpringBootApplication(scanBasePackages = {
        "com.crazypig.oh.proxy"
})
public class ProxyBootstrap {

    public static void main(String[] args) {
        SpringApplication.run(ProxyBootstrap.class, args);
    }

}
