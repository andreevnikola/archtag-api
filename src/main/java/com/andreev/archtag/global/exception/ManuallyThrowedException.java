package com.andreev.archtag.global.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public class ManuallyThrowedException {

    private String message;
    private HttpStatus httpStatusCodeException;
    private ZonedDateTime timestamp;

    public ManuallyThrowedException(String message) {
        initializeApiException(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ManuallyThrowedException(String message, HttpStatus httpStatusCodeException) {
        initializeApiException(message, httpStatusCodeException);
    }

    private void initializeApiException(String message, HttpStatus httpStatusCodeException) {
        this.message = (message != null) ? message : "Нещо се обърка! Моля, опитайте отново.";
        this.httpStatusCodeException = httpStatusCodeException;
        this.timestamp = ZonedDateTime.now();
    }

    public String getExceptionAsJson() throws JsonProcessingException {
        System.out.println("ManuallyThrowedException.getExceptionAsJson");

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());
        // Ensure the timestamps are serialized properly
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper.writeValueAsString(new ApiException(message, httpStatusCodeException));
    }
}