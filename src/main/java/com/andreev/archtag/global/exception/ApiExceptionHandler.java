package com.andreev.archtag.global.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {ApiRequestException.class})
    public ResponseEntity<Object> handleApiRequestException(ApiRequestException e) {
        return new ResponseEntity<>(new ApiException(
                (e.getMessage() != null) ? e.getMessage() : "Нещо се обърка! Моля, опитайте отново.",
                e,
                e.getStatus(),
                ZonedDateTime.now(ZoneId.of("Z"))
        ), e.getStatus());
    }
}
