package com.inventory.service;

import com.inventory.model.Item;
import com.inventory.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageMigrationService {

    private final ItemRepository itemRepository;
    private final ImageStorageService imageStorageService;
    private final ImageProcessingService imageProcessingService;

    @Transactional
    public void migrateItem(Item item) {
        byte[] imageData = item.getImageData();
        if (imageData == null || imageData.length == 0) {
            log.debug("Skipping image migration for item {}: no legacy image data present", item.getId());
            item.setImageData(null);
            item.setContentType(null);
            itemRepository.save(item);
            return;
        }

        byte[] webpBytes = imageProcessingService.processToWebP(imageData);
        String imageKey = "items/" + item.getId() + "/" + UUID.randomUUID() + ".webp";
        imageStorageService.upload(imageKey, webpBytes, "image/webp");
        item.setImageKey(imageKey);
        item.setImageData(null);
        item.setContentType(null);
        itemRepository.save(item);
    }
}
