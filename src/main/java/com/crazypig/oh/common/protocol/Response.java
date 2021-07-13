package com.crazypig.oh.common.protocol;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.CharsetUtil;
import lombok.Data;

/**
 * Created by chenjianxin on 2021/6/27.
 */
@Data
public class Response<T> {

    public static int MAX_FRAME_LENGTH = 1024 * 1024;

    private CommandType cmdType;

    private boolean success;

    private String message;

    private T data;

    public void fail(String message) {
        this.success = false;
        this.message = message;
    }

    public void success(T data) {
        this.success = true;
        this.data = data;
    }

    public String jsonString() {
        return JSON.toJSONString(this);
    }

    public ChannelFuture resp(Channel ch) {
        String jsonStr = jsonString();
        return ch.writeAndFlush(Unpooled.copiedBuffer(jsonStr, CharsetUtil.UTF_8));
    }

}
