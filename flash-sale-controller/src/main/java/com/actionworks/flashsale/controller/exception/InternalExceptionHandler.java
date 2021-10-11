package com.actionworks.flashsale.controller.exception;

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

import static com.actionworks.flashsale.controller.constants.ExceptionCode.INTERNAL_ERROR;

@ControllerAdvice
public class InternalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(InternalExceptionHandler.class);

    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    protected ResponseEntity<Object> handleConflict(Exception ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setErrorCode(INTERNAL_ERROR.getCode());
        exceptionResponse.setErrorMessage(INTERNAL_ERROR.getDesc());
        logger.error("Internal error occurred:{}", ex.getMessage(), ex);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(ex, JSON.toJSONString(exceptionResponse), httpHeaders
                , HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}