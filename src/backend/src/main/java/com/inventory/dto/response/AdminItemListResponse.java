package com.inventory.dto.response;

import com.inventory.model.ItemList;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminItemListResponse(
        UUID id,
        String name,
        String description,
        String category,
        int itemCount,
        UUID ownerId,
        String ownerEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminItemListResponse fromEntity(ItemList itemList, int itemCount) {
        return new AdminItemListResponse(
                itemList.getId(),
                itemList.getName(),
                itemList.getDescription(),
                itemList.getCategory(),
                itemCount,
                itemList.getUser().getId(),
                itemList.getUser().getEmail(),
                itemList.getCreatedAt(),
                itemList.getUpdatedAt()
        );
    }
}
