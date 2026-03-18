package com.inventory.dto.response;

import com.inventory.enums.InvitationStatus;
import com.inventory.enums.WorkspaceRole;
import com.inventory.model.WorkspaceInvitation;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkspaceInvitationResponse(
        UUID id,
        String email,
        WorkspaceRole role,
        InvitationStatus status,
        String workspaceName,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
    public static WorkspaceInvitationResponse fromEntity(WorkspaceInvitation invitation) {
        return new WorkspaceInvitationResponse(
                invitation.getId(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.getWorkspace().getName(),
                invitation.getCreatedAt(),
                invitation.getExpiresAt()
        );
    }
}
