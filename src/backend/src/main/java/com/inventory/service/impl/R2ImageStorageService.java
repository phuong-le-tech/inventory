package com.inventory.service.impl;

import com.inventory.exception.ImageStorageException;
import com.inventory.service.ImageStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "r2")
public class R2ImageStorageService implements ImageStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final Duration presignedUrlExpiry;

    public R2ImageStorageService(S3Client s3Client, S3Presigner s3Presigner,
                                  @Value("${app.storage.r2.bucket}") String bucket,
                                  @Value("${app.storage.r2.presigned-url-expiry-minutes:15}") int expiryMinutes) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.presignedUrlExpiry = Duration.ofMinutes(expiryMinutes);
    }

    @Override
    public String upload(String key, byte[] data, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(data));
            log.info("Uploaded image to R2: {}", key);
            return key;
        } catch (Exception e) {
            throw new ImageStorageException("Failed to upload image: " + key, e);
        }
    }

    @Override
    public String getPresignedUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(presignedUrlExpiry)
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build())
                    .build();
            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            throw new ImageStorageException("Failed to generate presigned URL for: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
            log.info("Deleted image from R2: {}", key);
        } catch (Exception e) {
            throw new ImageStorageException("Failed to delete image: " + key, e);
        }
    }
}
