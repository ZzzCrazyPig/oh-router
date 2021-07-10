package com.crazypig.oh.router.exception;

import com.crazypig.oh.common.exception.BizException;
import com.crazypig.oh.common.web.BaseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by chenjianxin on 2021/7/10.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BizException.class)
    public BaseResult bizExceptionHandler(HttpServletRequest request, BizException e) {
        log.error("BizError, code : " + e.getCode(), e);
        return BaseResult.notOk(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = Throwable.class)
    public BaseResult exceptionHandler(HttpServletRequest request, Throwable e) {
        log.error("UnknownError : " + e.getMessage(), e);
        return BaseResult.notOk(e.getMessage());
    }

}
