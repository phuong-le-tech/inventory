package com.inventory.service;

public interface ImageStorageService {

    String upload(String key, byte[] data, String contentType);

    String getPresignedUrl(String key);

    void delete(String key);
}
