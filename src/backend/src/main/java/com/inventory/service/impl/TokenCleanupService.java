package com.inventory.service.impl;

import com.inventory.repository.VerificationTokenRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private final VerificationTokenRepository verificationTokenRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        verificationTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
