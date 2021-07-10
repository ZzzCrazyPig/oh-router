package com.crazypig.oh.router.exception;

import com.crazypig.oh.common.exception.BizException;
import com.crazypig.oh.common.exception.ErrorCode;

/**
 * Created by chenjianxin on 2021/6/27.
 */
public class BindException extends BizException {

    public BindException(String message) {
        super(ErrorCode.BIND_ERROR, message);
    }

    public BindException(Throwable e) {
        this(e.getMessage(), e);
    }

    public BindException(String message, Throwable e) {
        super(ErrorCode.BIND_ERROR, message, e);
    }

}
