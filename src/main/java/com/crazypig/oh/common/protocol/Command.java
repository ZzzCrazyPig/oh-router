package com.crazypig.oh.common.protocol;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.CharsetUtil;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenjianxin on 2021/6/26.
 */
@Data
public class Command<T> implements Serializable {

    protected String sessionId;

    protected CommandType type;

    protected T data;

    public Command() {}

    public Command(String sessionId, CommandType type, T data) {
        this.sessionId = sessionId;
        this.type = type;
        this.data = data;
    }

    public static <T> Command<T> parse(String msg) {
        return JSON.parseObject(msg, Command.class);
    }

    public String jsonString() {
        return JSON.toJSONString(this);
    }

    public ChannelFuture send(Channel ch) {
        String jsonStr = jsonString();
        return ch.writeAndFlush(Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8));
    }

}
