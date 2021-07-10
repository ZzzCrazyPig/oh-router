package com.crazypig.oh.common.protocol;

/**
 * Created by chenjianxin on 2021/6/27.
 */
public class ConnectResponse extends Response<String> {

    public ConnectResponse() {
        setCmdType(CommandType.CONNECT);
    }

}
