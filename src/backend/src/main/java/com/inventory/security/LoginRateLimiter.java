package com.inventory.security;

import com.inventory.exception.RateLimitExceededException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final ConcurrentHashMap<String, List<Long>> attempts = new ConcurrentHashMap<>();

    public void checkRateLimit(String ip) {
        long now = System.currentTimeMillis();
        long windowStart = now - WINDOW_MS;

        List<Long> timestamps = attempts.compute(ip, (key, existing) -> {
            List<Long> list = (existing != null) ? existing : new ArrayList<>();
            list.removeIf(t -> t < windowStart);
            return list;
        });

        if (timestamps.size() >= MAX_ATTEMPTS) {
            throw new RateLimitExceededException("Too many login attempts. Please try again later.");
        }

        timestamps.add(now);
    }

    @Scheduled(fixedRate = 300_000) // every 5 minutes
    public void cleanup() {
        long windowStart = System.currentTimeMillis() - WINDOW_MS;
        attempts.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(t -> t < windowStart);
            return entry.getValue().isEmpty();
        });
    }
}
