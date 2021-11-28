package com.actionworks.flashsale.controller.exception;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExceptionResponse {
    private String errorCode;
    private String errorMessage;
}
