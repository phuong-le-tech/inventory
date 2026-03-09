package com.inventory.dto.response;

import com.inventory.model.Item;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record AdminItemResponse(
        UUID id,
        String name,
        String status,
        Integer stock,
        boolean hasImage,
        UUID listId,
        String listName,
        UUID ownerId,
        String ownerEmail,
        Map<String, Object> customFieldValues,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminItemResponse fromEntity(Item item) {
        return new AdminItemResponse(
                item.getId(),
                item.getName(),
                item.getStatus() != null ? item.getStatus().name() : null,
                item.getStock(),
                item.getImageData() != null && item.getImageData().length > 0,
                item.getItemList() != null ? item.getItemList().getId() : null,
                item.getItemList() != null ? item.getItemList().getName() : null,
                item.getItemList() != null ? item.getItemList().getUser().getId() : null,
                item.getItemList() != null ? item.getItemList().getUser().getEmail() : null,
                item.getCustomFieldValues() != null ? item.getCustomFieldValues() : Map.of(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
