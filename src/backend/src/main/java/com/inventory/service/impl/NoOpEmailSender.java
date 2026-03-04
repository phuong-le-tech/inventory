package com.inventory.service.impl;

import com.inventory.service.EmailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.email.provider", havingValue = "noop", matchIfMissing = true)
public class NoOpEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String htmlContent) {
        log.info("=== EMAIL (NoOp) ===");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Content: [HTML redacted — {} chars]", htmlContent.length());
        log.info("====================");
    }
}
