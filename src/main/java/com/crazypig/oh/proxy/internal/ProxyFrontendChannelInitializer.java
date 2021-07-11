package com.crazypig.oh.proxy.internal;

import com.crazypig.oh.proxy.ProxyServer;
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
public class ProxyFrontendChannelInitializer extends ChannelInitializer {

    private static final Pair<String, Supplier<ChannelHandler>> LEN_FIELD_ENCODER =
            Pair.of("lengthFieldEncoder", () -> new LengthFieldPrepender(4));

    private static final Pair<String, Supplier<ChannelHandler>> LEN_FIELD_DECODER =
            Pair.of("lengthFieldDecoder", () -> new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));

    private static final Pair<String, Supplier<ChannelHandler>> STRING_DECODER =
            Pair.of("stringDecoder", () -> new StringDecoder(CharsetUtil.UTF_8));

    private ProxyServer proxyServer;

    public ProxyFrontendChannelInitializer(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        codec(channel);
        channel.pipeline().addLast("proxyFrontendHandler", new ProxyFrontendHandler(this.proxyServer));
    }

    static void codec(Channel channel) {

        channel.pipeline().addLast(LEN_FIELD_ENCODER.getKey(), LEN_FIELD_ENCODER.getValue().get());
        channel.pipeline().addLast(LEN_FIELD_DECODER.getKey(), LEN_FIELD_DECODER.getValue().get());
        channel.pipeline().addLast(STRING_DECODER.getKey(), STRING_DECODER.getValue().get());
    }

    static void unCodec(Channel channel) {

        channel.pipeline().remove(LEN_FIELD_ENCODER.getKey());
        channel.pipeline().remove(LEN_FIELD_DECODER.getKey());
        channel.pipeline().remove(STRING_DECODER.getKey());

    }

}
