package com.inventory.dto.response;

import com.inventory.enums.WorkspaceRole;
import com.inventory.model.Workspace;
import java.time.LocalDateTime;
import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        String name,
        boolean isDefault,
        WorkspaceRole role,
        long memberCount,
        long listCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static WorkspaceResponse fromEntity(Workspace workspace, WorkspaceRole currentUserRole, long memberCount, long listCount) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.isDefault(),
                currentUserRole,
                memberCount,
                listCount,
                workspace.getCreatedAt(),
                workspace.getUpdatedAt()
        );
    }
}
