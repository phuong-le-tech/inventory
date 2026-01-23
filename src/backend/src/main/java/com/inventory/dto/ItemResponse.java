package com.inventory.dto;

import com.inventory.model.Item;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Data
public class ItemResponse {

    private UUID id;
    private String name;
    private String category;
    private String status;
    private String imageBase64;
    private String contentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ItemResponse fromEntity(Item item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setCategory(item.getCategory());
        response.setStatus(item.getStatus());
        response.setContentType(item.getContentType());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());

        if (item.getImageData() != null) {
            response.setImageBase64(Base64.getEncoder().encodeToString(item.getImageData()));
        }

        return response;
    }
}
