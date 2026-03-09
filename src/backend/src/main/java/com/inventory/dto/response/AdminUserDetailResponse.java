package com.inventory.dto.response;

import com.inventory.enums.Role;
import com.inventory.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserDetailResponse(
    UUID id,
    String email,
    Role role,
    String pictureUrl,
    boolean hasGoogleAccount,
    boolean enabled,
    long listCount,
    long itemCount,
    boolean hasStripePayment,
    String stripeCustomerId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static AdminUserDetailResponse fromEntity(User user, long listCount, long itemCount) {
        return new AdminUserDetailResponse(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.getPictureUrl(),
            user.getGoogleId() != null,
            user.isEnabled(),
            listCount,
            itemCount,
            user.getStripePaymentId() != null,
            user.getStripeCustomerId(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
