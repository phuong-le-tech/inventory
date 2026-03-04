package com.inventory.service;

public interface EmailSender {
    void send(String to, String subject, String htmlContent);
}
