package com.inventory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.security.ApiRateLimitFilter;
import com.inventory.security.ApiRateLimiter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class RateLimitConfig {

    @Bean
    public ApiRateLimiter apiRateLimiter() {
        return new ApiRateLimiter(100, 60_000); // 100 requests per minute
    }

    @Bean
    public FilterRegistrationBean<ApiRateLimitFilter> apiRateLimitFilter(
            ApiRateLimiter apiRateLimiter,
            ObjectMapper objectMapper
    ) {
        FilterRegistrationBean<ApiRateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiRateLimitFilter(apiRateLimiter, objectMapper));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}
