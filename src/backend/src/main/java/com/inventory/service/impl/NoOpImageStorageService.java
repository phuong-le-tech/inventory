package com.inventory.service.impl;

import com.inventory.service.ImageStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "noop", matchIfMissing = true)
public class NoOpImageStorageService implements ImageStorageService {

    @Override
    public String upload(String key, byte[] data, String contentType) {
        log.debug("NoOp image upload: key={}, size={}", key, data.length);
        return key;
    }

    @Override
    public String getPresignedUrl(String key) {
        return null;
    }

    @Override
    public void delete(String key) {
        log.debug("NoOp image delete: key={}", key);
    }
}
