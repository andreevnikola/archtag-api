package com.andreev.archtag.global.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApiException extends RuntimeException {

    private String message;
    private Throwable cause;
    private HttpStatus httpStatus;
    private ZonedDateTime timestamp;

    public ApiException(String message, Throwable cause, HttpStatus httpStatus) {
        initializeApiException(message, cause, httpStatus);
    }

    public ApiException(String message, HttpStatus httpStatus) {
        initializeApiException(message, null, httpStatus);
    }

    private void initializeApiException(String message, Throwable cause, HttpStatus httpStatus) {
        this.message = (message != null) ? message : "Нещо се обърка! Моля, опитайте отново.";
        this.cause = cause;
        this.httpStatus = httpStatus;
        this.timestamp = ZonedDateTime.now();
    }
}