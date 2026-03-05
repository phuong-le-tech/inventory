package com.inventory.service;

import org.springframework.lang.NonNull;

public interface EmailSender {
    void send(@NonNull String to, @NonNull String subject, @NonNull String htmlContent);
}
