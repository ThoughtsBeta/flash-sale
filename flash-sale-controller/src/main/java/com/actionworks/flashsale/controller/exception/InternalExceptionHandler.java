package com.actionworks.flashsale.controller.exception;

import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.lang.reflect.UndeclaredThrowableException;

import static com.actionworks.flashsale.controller.constants.ExceptionCode.INTERNAL_ERROR;
import static com.actionworks.flashsale.controller.constants.ExceptionCode.LIMIT_BLOCK;

@ControllerAdvice
public class InternalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(InternalExceptionHandler.class);

    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    protected ResponseEntity<Object> handleConflict(Exception ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        if (ex instanceof UndeclaredThrowableException) {
            if (((UndeclaredThrowableException) ex).getUndeclaredThrowable() instanceof FlowException) {
                exceptionResponse.setErrorCode(LIMIT_BLOCK.getCode());
                exceptionResponse.setErrorMessage(LIMIT_BLOCK.getDesc());
            }
        } else {
            exceptionResponse.setErrorCode(INTERNAL_ERROR.getCode());
            exceptionResponse.setErrorMessage(INTERNAL_ERROR.getDesc());
        }

        logger.error("unknownError|未知错误|{}", ex.getClass());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(ex, JSON.toJSONString(exceptionResponse), httpHeaders
                , HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}