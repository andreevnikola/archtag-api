package com.andreev.archtag.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {ApiRequestException.class})
    public ResponseEntity<ApiException> handleApiRequestException(ApiRequestException e) {
        return new ResponseEntity<>(new ApiException(
                e.getMessage(),
                e,
                e.getStatus()
        ), e.getStatus());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiException> handleNoHandlerFoundException(NoHandlerFoundException e) {
        return new ResponseEntity<>(new ApiException(
                "Page not found",
                HttpStatus.NOT_FOUND
        ), HttpStatus.NOT_FOUND);
    }
}