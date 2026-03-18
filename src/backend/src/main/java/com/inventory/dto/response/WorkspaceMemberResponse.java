package com.inventory.dto.response;

import com.inventory.enums.WorkspaceRole;
import com.inventory.model.WorkspaceMember;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkspaceMemberResponse(
        UUID id,
        UUID userId,
        String email,
        String pictureUrl,
        WorkspaceRole role,
        LocalDateTime joinedAt
) {
    public static WorkspaceMemberResponse fromEntity(WorkspaceMember member) {
        return new WorkspaceMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getUser().getPictureUrl(),
                member.getRole(),
                member.getCreatedAt()
        );
    }
}
