package com.inventory.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DashboardStats {

    private long totalItems;
    private Map<String, Long> countByStatus;
    private Map<String, Long> countByCategory;
}
