package com.andreev.archtag.global.services;

public interface EmailService {
    void send(String to, String subject, String text);
}
