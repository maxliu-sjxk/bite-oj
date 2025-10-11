package com.bite.common.message.config;

import com.bite.common.message.service.Mail;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MailConfig {

//    @ConditionalOnProperty(prefix = "spring.mail", name = "username")
    @Bean
    public Mail mail(JavaMailSender javaMailSender, MailProperties mailProperties) {
        return new Mail(javaMailSender, mailProperties);
    }
}
