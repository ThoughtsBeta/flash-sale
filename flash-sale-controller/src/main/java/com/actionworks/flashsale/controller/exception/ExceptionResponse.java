package com.actionworks.flashsale.controller.exception;

import lombok.Data;

@Data
public class ExceptionResponse {
    private String errorCode;
    private String errorMessage;
}
