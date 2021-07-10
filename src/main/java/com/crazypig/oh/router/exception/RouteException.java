package com.crazypig.oh.router.exception;

import com.crazypig.oh.common.exception.BizException;
import com.crazypig.oh.common.exception.ErrorCode;

/**
 * Created by chenjianxin on 2021/6/27.
 */
public class RouteException extends BizException {

    public RouteException(String message) {
        super(ErrorCode.ROUTE_ERROR, message);
    }

    public RouteException(Throwable e) {
        this(e.getMessage(), e);
    }

    public RouteException(String message, Throwable e) {
        super(ErrorCode.ROUTE_ERROR, message, e);
    }
}
