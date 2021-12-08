package com.actionworks.flashsale.controller.exception;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.lang.reflect.UndeclaredThrowableException;

import static com.actionworks.flashsale.controller.constants.ExceptionCode.DEGRADE_BLOCK;
import static com.actionworks.flashsale.controller.constants.ExceptionCode.LIMIT_BLOCK;

public class SentinelExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SentinelExceptionHandler.class);

    @ExceptionHandler(value = {UndeclaredThrowableException.class})
    protected ResponseEntity<Object> handleConflict(UndeclaredThrowableException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        if (ex.getUndeclaredThrowable() instanceof FlowException) {
            exceptionResponse.setErrorCode(LIMIT_BLOCK.getCode());
            exceptionResponse.setErrorMessage(LIMIT_BLOCK.getDesc());
        }
        if (ex.getUndeclaredThrowable() instanceof DegradeException) {
            exceptionResponse.setErrorCode(DEGRADE_BLOCK.getCode());
            exceptionResponse.setErrorMessage(DEGRADE_BLOCK.getDesc());
        }
        logger.error("expectedException|预期错误|{},{}", ex.getMessage(), ex);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(ex, JSON.toJSONString(exceptionResponse), httpHeaders, HttpStatus.BAD_REQUEST, request);
    }
}