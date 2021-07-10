package com.crazypig.oh.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by chenjianxin on 2020/5/22.
 */
@SpringBootApplication(scanBasePackages = {
        "com.crazypig.oh.router"
})
public class RouterBootstrap {

    public static void main(String[] args) {
        SpringApplication.run(RouterBootstrap.class, args);
    }

}
