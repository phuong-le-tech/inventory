package com.inventory.service;

import com.inventory.dto.DashboardStats;
import com.inventory.dto.ItemRequest;
import com.inventory.model.Item;
import com.inventory.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Item getItemById(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
    }

    public Item createItem(ItemRequest request, MultipartFile image) throws IOException {
        Item item = new Item();
        item.setName(request.getName());
        item.setCategory(request.getCategory());
        item.setStatus(request.getStatus() != null ? request.getStatus() : "In Stock");

        if (image != null && !image.isEmpty()) {
            item.setImageData(image.getBytes());
            item.setContentType(image.getContentType());
        }

        return itemRepository.save(item);
    }

    public Item updateItem(UUID id, ItemRequest request, MultipartFile image) throws IOException {
        Item item = getItemById(id);
        item.setName(request.getName());
        item.setCategory(request.getCategory());
        item.setStatus(request.getStatus());

        if (image != null && !image.isEmpty()) {
            item.setImageData(image.getBytes());
            item.setContentType(image.getContentType());
        }

        return itemRepository.save(item);
    }

    public void deleteItem(UUID id) {
        itemRepository.deleteById(id);
    }

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalItems(itemRepository.count());

        Map<String, Long> statusCounts = itemRepository.countByStatus().stream()
                .collect(Collectors.toMap(
                        row -> row[0] != null ? (String) row[0] : "Unknown",
                        row -> (Long) row[1]
                ));
        stats.setCountByStatus(statusCounts);

        Map<String, Long> categoryCounts = itemRepository.countByCategory().stream()
                .collect(Collectors.toMap(
                        row -> row[0] != null ? (String) row[0] : "Uncategorized",
                        row -> (Long) row[1]
                ));
        stats.setCountByCategory(categoryCounts);

        return stats;
    }
}
