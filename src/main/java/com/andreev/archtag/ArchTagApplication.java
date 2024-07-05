package com.andreev.archtag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ArchTagApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchTagApplication.class, args);
    }

    // Remove the corsConfigurer method to disable CORS configuration
}