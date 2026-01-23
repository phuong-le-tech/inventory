package com.inventory.controller;

import com.inventory.dto.DashboardStats;
import com.inventory.dto.ItemRequest;
import com.inventory.dto.ItemResponse;
import com.inventory.model.Item;
import com.inventory.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/items")
    public List<ItemResponse> getAllItems() {
        return itemService.getAllItems().stream()
                .map(ItemResponse::fromEntity)
                .toList();
    }

    @GetMapping("/items/{id}")
    public ItemResponse getItem(@PathVariable UUID id) {
        return ItemResponse.fromEntity(itemService.getItemById(id));
    }

    @PostMapping(value = "/items", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponse> createItem(
            @RequestPart("data") @Valid ItemRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        Item item = itemService.createItem(request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(ItemResponse.fromEntity(item));
    }

    @PutMapping(value = "/items/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ItemResponse updateItem(
            @PathVariable UUID id,
            @RequestPart("data") @Valid ItemRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        Item item = itemService.updateItem(id, request, image);
        return ItemResponse.fromEntity(item);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard/stats")
    public DashboardStats getDashboardStats() {
        return itemService.getDashboardStats();
    }
}
