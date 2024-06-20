package com.andreev.archtag.global.exception;

import lombok.Getter;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class ApiRequestException extends RuntimeException {

    private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public ApiRequestException(HttpStatus status) {
        this.status = status;
    }

    public ApiRequestException(String message) {
        super(message);
    }

    public ApiRequestException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ApiRequestException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public ResponseEntity<ApiException> ExceptionAsResponse() {
        return ResponseEntity.status(this.status).body(new ApiException(this.getMessage(), this.getCause(), this.getStatus()));
    }
}
