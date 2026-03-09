package com.inventory.controller;

import com.inventory.exception.RateLimitExceededException;
import com.inventory.security.ApiRateLimiter;
import com.inventory.security.CustomUserDetails;
import com.inventory.service.IAuthService;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private final IAuthService authService;
    private final ApiRateLimiter accountDeletionRateLimiter;

    public AccountController(IAuthService authService,
                             @Qualifier("accountDeletionRateLimiter") ApiRateLimiter accountDeletionRateLimiter) {
        this.authService = authService;
        this.accountDeletionRateLimiter = accountDeletionRateLimiter;
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) {
        if (!accountDeletionRateLimiter.tryAcquire("delete:user:" + userDetails.getId()).allowed()) {
            throw new RateLimitExceededException("Too many deletion requests. Please try again later.");
        }
        log.info("SECURITY: User {} ({}) requesting account deletion", userDetails.getId(), userDetails.getEmail());
        authService.deleteAccount(userDetails.getId(), response);
        log.info("SECURITY: Account deleted for user {} ({})", userDetails.getId(), userDetails.getEmail());
        return ResponseEntity.noContent().build();
    }
}
