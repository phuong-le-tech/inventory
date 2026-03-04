package com.inventory.security;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;

public class ApiRateLimiter {

    private static final int MAX_ENTRIES = 50_000;

    private final int maxRequests;
    private final long windowMs;
    private final ConcurrentHashMap<String, List<Long>> requests = new ConcurrentHashMap<>();

    public ApiRateLimiter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    /**
     * Returns true if the request is allowed, false if rate limit exceeded.
     */
    public boolean isAllowed(String key) {
        if (requests.size() >= MAX_ENTRIES) {
            cleanup();
        }
        if (requests.size() >= MAX_ENTRIES) {
            return false;
        }

        long now = System.currentTimeMillis();
        long windowStart = now - windowMs;

        List<Long> timestamps = requests.compute(key, (k, existing) -> {
            List<Long> list = (existing != null) ? existing : new ArrayList<>();
            list.removeIf(t -> t < windowStart);
            return list;
        });

        if (timestamps.size() >= maxRequests) {
            return false;
        }

        timestamps.add(now);
        return true;
    }

    @Scheduled(fixedRate = 300_000)
    public void cleanup() {
        long windowStart = System.currentTimeMillis() - windowMs;
        requests.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(t -> t < windowStart);
            return entry.getValue().isEmpty();
        });
    }
}
