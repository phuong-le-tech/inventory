package com.inventory.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the real client IP address from a request.
 * Relies on {@code server.forward-headers-strategy=NATIVE} which configures
 * Tomcat's RemoteIpValve to rewrite {@code getRemoteAddr()} for trusted proxies.
 * No manual X-Forwarded-For parsing — that would allow IP spoofing to bypass rate limits.
 */
public final class ClientIpResolver {

    private ClientIpResolver() {}

    public static String resolve(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
