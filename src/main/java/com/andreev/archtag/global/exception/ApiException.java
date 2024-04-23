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
public class ApiException {

    private final String message;
    private final Throwable cause;
    private final HttpStatus httpStatus;
    private  final ZonedDateTime timestamp;
}
