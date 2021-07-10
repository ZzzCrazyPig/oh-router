package com.crazypig.oh.common.protocol;


import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * Created by chenjianxin on 2021/6/26.
 */
@EqualsAndHashCode
public class RouteInfo extends Address {

    public RouteInfo(String host, int port) {
        super(host, port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHost(), getPort());
    }

}
