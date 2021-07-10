package com.crazypig.oh.common.web;

import com.crazypig.oh.common.exception.ErrorCode;
import lombok.Data;

/**
 * Created by chenjianxin on 2021/6/27.
 */
@Data
public class BaseResult<T> {

    private boolean success;

    private T data;

    private ErrorCode code;

    private String message;

    public static <T> BaseResult ok(T data) {
        BaseResult baseResult = new BaseResult();
        baseResult.setSuccess(true);
        baseResult.setData(data);
        return baseResult;
    }

    public static BaseResult notOk(String message) {
        BaseResult baseResult = new BaseResult();
        baseResult.setSuccess(false);
        baseResult.setMessage(message);
        baseResult.setCode(ErrorCode.UNKNOWN_ERROR);
        return baseResult;
    }

    public static BaseResult notOk(ErrorCode code, String message) {
        BaseResult baseResult = new BaseResult();
        baseResult.setSuccess(false);
        baseResult.setMessage(message);
        baseResult.setCode(code);
        return baseResult;
    }

}
