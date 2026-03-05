package com.inventory.security;

import com.inventory.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final ApiRateLimiter rateLimiter = new ApiRateLimiter(MAX_ATTEMPTS, WINDOW_MS);

    public void checkRateLimit(HttpServletRequest request) {
        String ip = ClientIpResolver.resolve(request);
        ApiRateLimiter.RateLimitResult result = rateLimiter.tryAcquire(ip);
        if (!result.allowed()) {
            throw new RateLimitExceededException("Too many login attempts. Please try again later.");
        }
    }
}
