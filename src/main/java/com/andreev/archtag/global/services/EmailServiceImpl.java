package com.andreev.archtag.global.services;

import com.andreev.archtag.global.utils.ConfigUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private ConfigUtility configUtility;

    public void send(
            String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(configUtility.getProperty("spring.mail.username"));
//        System.out.println("Sending email from: " + configUtility.getProperty("spring.mail.username"));
//        System.out.println("Sending email to: " + to);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}
