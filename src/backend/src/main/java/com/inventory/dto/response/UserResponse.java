package com.inventory.dto.response;

import com.inventory.enums.Role;
import com.inventory.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    Role role,
    String pictureUrl,
    boolean hasGoogleAccount,
    boolean enabled,
    LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.getPictureUrl(),
            user.getGoogleId() != null,
            user.isEnabled(),
            user.getCreatedAt()
        );
    }
}
