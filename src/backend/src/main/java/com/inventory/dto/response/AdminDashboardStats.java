package com.inventory.dto.response;

import java.util.List;
import java.util.Map;

public record AdminDashboardStats(
        long totalUsers,
        long activeUsers,
        long premiumUsers,
        long adminUsers,
        double premiumConversionRate,
        Map<String, Long> registrationTrend,
        List<UserStat> topUsersByLists,
        List<UserStat> topUsersByItems
) {
    public record UserStat(String email, long count) {}
}
