package com.crazypig.oh.common.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by chenjianxin on 2021/6/30.
 */
@Data
@AllArgsConstructor
public class Address {

    private String host;

    private int port;

    public String toString() {
        return this.host + ":" + this.port;
    }

}
