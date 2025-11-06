package com.idea.authservice.patterns.decorator;

import com.idea.authservice.patterns.observer.BasicNotifier;
import com.idea.authservice.patterns.observer.EmailNotifier;
import com.idea.authservice.patterns.decorator.WhatsAppNotifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class NotifierConfig {

    @Bean
    public INotifier notifier(JavaMailSender mailSender,
                              @Value("${spring.mail.username}") String fromAddress) {
        return new WhatsAppNotifier(new EmailNotifier(new BasicNotifier(), mailSender, fromAddress));
    }
}
