package com.crazypig.oh.router.internal;

import com.crazypig.oh.common.protocol.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;


/**
 * Created by chenjianxin on 2021/7/4.
 */
public class RouteBackendChannelInitializer extends ChannelInitializer {

    public static final RouteBackendChannelInitializer INSTANCE = new RouteBackendChannelInitializer();

    private static final Pair<String, Supplier<ChannelHandler>> COM_ENCODER =
            Pair.of("commandEncoder", () -> new LengthFieldPrepender(3));

    private static final Pair<String, Supplier<ChannelHandler>> RESP_DECODER =
            Pair.of("responseDecoder", () -> new LengthFieldBasedFrameDecoder(Response.MAX_FRAME_LENGTH, 0, 3, 0, 3));

    private static final Pair<String, Supplier<ChannelHandler>> STRING_DECODER =
            Pair.of("stringDecoder", () -> new StringDecoder(CharsetUtil.UTF_8));

    @Override
    protected void initChannel(Channel channel) throws Exception {
        codec(channel);
        channel.pipeline().addLast("routeBackendHandler", new RouteBackendHandler());
    }

    static void codec(Channel channel) {
        channel.pipeline().addLast(COM_ENCODER.getKey(), COM_ENCODER.getValue().get());
        channel.pipeline().addLast(RESP_DECODER.getKey(), RESP_DECODER.getValue().get());
        channel.pipeline().addLast(STRING_DECODER.getKey(), STRING_DECODER.getValue().get());
    }

    static void uncodec(Channel channel) {
        channel.pipeline().remove(COM_ENCODER.getKey());
        channel.pipeline().remove(RESP_DECODER.getKey());
        channel.pipeline().remove(STRING_DECODER.getKey());
    }

}
