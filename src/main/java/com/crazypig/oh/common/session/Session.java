package com.crazypig.oh.common.session;

/**
 * Created by chenjianxin on 2021/6/29.
 * 链路会话
 */
public interface Session<T> {

    /**
     * 会话ID
     * @return
     */
    String id();

    /**
     * 前端
     * @return
     */
    T frontend();

    /**
     * 后端
     * @return
     */
    T backend();


    /**
     * 链路会话是否已经建立
     * @return
     */
    boolean establish();


    /**
     * 会话关闭
     */
    void close();

}
