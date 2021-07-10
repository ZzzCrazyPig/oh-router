package com.crazypig.oh.common.protocol;

import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * Created by chenjianxin on 2021/6/26.
 */
@Data
public class AckCommand extends Command<String> {

    public AckCommand(String sessionId) {
        super(sessionId, CommandType.ACK, sessionId);
    }

    public AckCommand() {

    }

    public static AckCommand parse(String msg) {
        return JSON.parseObject(msg, AckCommand.class);
    }

}
