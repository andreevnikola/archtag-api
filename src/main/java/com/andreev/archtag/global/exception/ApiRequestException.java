package com.andreev.archtag.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiRequestException extends RuntimeException {

    private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public ApiRequestException(HttpStatus status) {
        this.status = status;
    }

    public ApiRequestException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ApiRequestException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
