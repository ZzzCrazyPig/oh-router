package com.crazypig.oh.common.exception;

import lombok.Data;

/**
 * Created by chenjianxin on 2021/7/10.
 */
@Data
public class BizException extends RuntimeException {

    private ErrorCode code;

    public BizException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ErrorCode code, String message, Throwable e) {
        super(message, e);
        this.code = code;
    }

}
