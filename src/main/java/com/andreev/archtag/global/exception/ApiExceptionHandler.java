package com.andreev.archtag.global.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {ApiRequestException.class})
    public ResponseEntity<Object> handleApiRequestException(ApiRequestException e) {
        return new ResponseEntity<>(new ApiException(
                e.getMessage(),
                e,
                e.getStatus()
        ), e.getStatus());
    }
}
