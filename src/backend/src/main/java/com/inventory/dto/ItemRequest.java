package com.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String category;

    private String status;
}
