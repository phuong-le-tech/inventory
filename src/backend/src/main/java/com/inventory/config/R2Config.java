package com.inventory.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "r2")
public class R2Config {

    @Value("${app.storage.r2.endpoint}")
    private String endpoint;

    @Value("${app.storage.r2.access-key}")
    private String accessKey;

    @Value("${app.storage.r2.secret-key}")
    private String secretKey;

    @Value("${app.storage.r2.bucket}")
    private String bucket;

    @Value("${app.storage.r2.region:auto}")
    private String region;

    private final Environment environment;

    public R2Config(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            if (endpoint == null || endpoint.isBlank()
                    || accessKey == null || accessKey.isBlank()
                    || secretKey == null || secretKey.isBlank()
                    || bucket == null || bucket.isBlank()) {
                throw new IllegalStateException(
                        "R2 storage provider is set to 'r2' but required configuration is missing. "
                        + "Set R2_ENDPOINT, R2_ACCESS_KEY, R2_SECRET_KEY, and R2_BUCKET environment variables.");
            }
            validateEndpoint(endpoint);
        }
        log.info("R2 storage configured: endpoint={}, bucket={}", endpoint, bucket);
    }

    private void validateEndpoint(String endpoint) {
        try {
            URI uri = URI.create(endpoint);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalStateException("R2 endpoint must use HTTPS in production: " + endpoint);
            }
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                throw new IllegalStateException("R2 endpoint has no valid host: " + endpoint);
            }
            if (host.equals("localhost") || host.equals("127.0.0.1") || host.startsWith("10.")
                    || host.startsWith("192.168.") || host.startsWith("172.")) {
                throw new IllegalStateException("R2 endpoint must not point to internal/private addresses in production: " + endpoint);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("R2 endpoint is not a valid URI: " + endpoint, e);
        }
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
