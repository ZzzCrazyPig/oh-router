package com.crazypig.oh.common.util;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * Created by chenjianxin on 2021/7/14.
 */
public class ChannelUtils {

    public static void close(Channel channel) {
        if (channel == null) {
            return;
        }
        if (channel.isActive()) {
            channel.close();
        }
    }

}
