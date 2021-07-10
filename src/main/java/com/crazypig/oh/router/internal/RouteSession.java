package com.crazypig.oh.router.internal;

import com.alibaba.fastjson.JSON;
import com.crazypig.oh.common.protocol.*;
import com.crazypig.oh.common.session.ChannelSession;
import com.crazypig.oh.router.exception.RouteException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Created by chenjianxin on 2021/7/4.
 */
@Slf4j
public class RouteSession extends ChannelSession implements RouteSessionHandler {

    private volatile RouteSessionState state = RouteSessionState.INIT;

    private RouteInfo routeInfo;

    private String proxySessionId;

    private Promise<RouteSession> routePromise;

    public RouteSession(RouteInfo routeInfo, Promise<RouteSession> routePromise,
                        Channel frontendChannel, Channel backendChannel) {
        super(frontendChannel, backendChannel);
        this.routeInfo = routeInfo;
        this.routePromise = routePromise;
    }

    @Override
    public ChannelFuture handleRoute() {
        // 发送connect command 请求连接对端
        String sessionId = id();
        ConnectCommand connectCommand = new ConnectCommand(sessionId, routeInfo);
        log.info("session [{}] will send route command : {}", sessionId, connectCommand.jsonString());
        return connectCommand.send(backend());
    }

    /**
     * 发送ack message 给 proxy
     * @return
     */
    ChannelFuture ack() {
        // 这里还需要再发送一个确认包
        String sessionId = id();
        AckCommand ackCommand = new AckCommand(sessionId);
        log.info("session [{}] send established command : {}", id(), ackCommand.jsonString());
        return ackCommand.send(backend());
    }

    @Override
    public void handleRead(ChannelHandlerContext ctx, Object msg) {

        Channel channel = ctx.channel();

        if (channel == frontend()) {
            handleFrontendRead(msg);
            return;
        }

        if (channel == backend()) {
            handleBackendRead(msg);
            return;
        }

        throw new IllegalStateException("Unknown channel [" + channel.id() + "] of route session : [" + toString() + "]");

    }

    private void handleFrontendRead(Object msg) {
        Channel backendChannel = backend();
        log.debug("session [{}] frontend channel [{}] receive data : \n {}",
                id(), backendChannel.id(), ByteBufUtil.prettyHexDump((ByteBuf) msg));
        backendChannel.writeAndFlush(msg);
    }

    private void handleBackendRead(Object msg) {

        Channel backendChannel = backend();

        log.debug("session [{}] backend channel [{}] receive data, state : [{}]", id(), backendChannel.id(), state);

        // 已经链接上, 后续对数据的处理直接写到对应的frontend channel
        if (establish()) {

            log.debug("session [{}] backend channel [{}] receive data \n : {}", id(), backendChannel.id(),
                    ByteBufUtil.prettyHexDump((ByteBuf) msg));

            Channel frontendChannel = frontend();
            frontendChannel.writeAndFlush(msg);
            return;
        }

        String message = (String) msg;
        log.info("session [{}] backend channel [{}] receive response : {}", id(), backendChannel.id(), message);
        Response response = JSON.parseObject(message, Response.class);

        // 接受到无效的消息, 异常处理
        if (response.getCmdType() != CommandType.CONNECT) {
            RouteException ex = new RouteException("Unknown response : " + message);
            routePromise.setFailure(ex);
            return;
        }

        ConnectResponse connectResponse = JSON.parseObject(message, ConnectResponse.class);

        if (!connectResponse.isSuccess()) {
            RouteException ex = new RouteException(response.getMessage());
            this.onRouteFail(ex);
            return;
        }

        // 接收到成功连上的返回以后, 还需要再发一个ack请求
        ChannelFuture ackFuture = ack();

        ackFuture.addListener(f -> {

            if (!f.isSuccess()) {
                onRouteFail(f.cause());
                return;
            }

            this.proxySessionId = connectResponse.getData();
            this.onEstablished();

        });

    }



    @Override
    public void onEstablished() {

        // 后端channel的pipeline重新装配
        Channel backendChannel = backend();
        RouteBackendChannelInitializer.uncodec(backendChannel);

        this.state = RouteSessionState.ESTABLISHED;

        // promise成功通知
        routePromise.setSuccess(this);
    }

    @Override
    public void onRouteFail(Throwable cause) {
        // promise异常通知
        routePromise.setFailure(cause);
    }

    @Override
    public boolean establish() {
        return this.state == RouteSessionState.ESTABLISHED;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(" " + this.state);
        sb.append(" => ").append(Optional.ofNullable(proxySessionId).orElse("NULL"));
        return sb.toString();
    }

}
