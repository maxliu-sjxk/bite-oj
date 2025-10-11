package com.bite.common.message.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.Optional;

public class Mail {

    private JavaMailSender javaMailSender;
    private MailProperties mailProperties;

    public Mail(JavaMailSender javaMailSender, MailProperties mailProperties) {
        this.javaMailSender = javaMailSender;
        this.mailProperties = mailProperties;
    }

    public void send(String to, String subject, String content) throws Exception {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
        String personal = Optional.ofNullable(mailProperties.getProperties().get("personal"))
                .orElse(mailProperties.getUsername());
        helper.setFrom(mailProperties.getUsername(), personal);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        javaMailSender.send(mimeMessage);
    }

}
