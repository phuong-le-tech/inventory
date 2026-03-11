package com.inventory.config;

import com.inventory.model.Item;
import com.inventory.repository.ItemRepository;
import com.inventory.service.ImageMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.migrate-images", havingValue = "true")
public class ImageMigrationRunner implements ApplicationRunner {

    private static final int BATCH_SIZE = 50;

    private final ItemRepository itemRepository;
    private final ImageMigrationService imageMigrationService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting legacy image migration to R2...");

        List<Item> legacyItems = itemRepository.findItemsWithLegacyImages();
        if (legacyItems == null || legacyItems.isEmpty()) {
            log.info("No legacy images found to migrate");
            return;
        }
        log.info("Found {} items with legacy images to migrate", legacyItems.size());

        int migrated = 0;
        int failed = 0;

        for (int i = 0; i < legacyItems.size(); i += BATCH_SIZE) {
            List<Item> batch = legacyItems.subList(i, Math.min(i + BATCH_SIZE, legacyItems.size()));
            for (Item item : batch) {
                try {
                    imageMigrationService.migrateItem(item);
                    migrated++;
                } catch (Exception e) {
                    failed++;
                    log.error("Failed to migrate image for item {}: {}", item.getId(), e.getMessage());
                }
            }
            log.info("Progress: {}/{} migrated, {} failed", migrated, legacyItems.size(), failed);
        }

        log.info("Image migration completed: {} migrated, {} failed out of {} total",
                migrated, failed, legacyItems.size());
    }
}
