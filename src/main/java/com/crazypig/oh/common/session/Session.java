package com.crazypig.oh.common.session;

/**
 * Created by chenjianxin on 2021/6/29.
 *
 */
public interface Session<T> {

    /**
     * session ID
     * @return
     */
    String id();

    /**
     * session frontend
     * @return
     */
    T frontend();

    /**
     * session backend
     * @return
     */
    T backend();


    /**
     * session is established or not
     * @return
     */
    boolean established();


    /**
     * close session
     */
    void close();

}
