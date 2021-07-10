package com.crazypig.oh.common.protocol;

import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * Created by chenjianxin on 2021/6/26.
 */
@Data
public class ConnectCommand extends Command<RouteInfo> {

    public ConnectCommand(String sessionId, RouteInfo routeInfo) {
        super(sessionId, CommandType.CONNECT, routeInfo);
    }

    public ConnectCommand() {
    }

    public Address target() {
        return new Address(getData().getHost(), getData().getPort());
    }

    public static ConnectCommand parse(String msg) {
        return JSON.parseObject(msg, ConnectCommand.class);
    }

}
