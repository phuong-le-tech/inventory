package com.inventory.security;

import com.inventory.enums.WorkspaceRole;
import com.inventory.exception.UnauthorizedException;
import com.inventory.exception.WorkspaceAccessDeniedException;
import com.inventory.exception.WorkspaceNotFoundException;
import com.inventory.model.WorkspaceMember;
import com.inventory.repository.WorkspaceMemberRepository;
import com.inventory.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WorkspaceAccessUtils {

    private final SecurityUtils securityUtils;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;

    public UUID requireCurrentUserId() {
        return securityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
    }

    /**
     * Verify the current user is a member of the workspace and return the membership.
     */
    public WorkspaceMember requireMembership(UUID workspaceId) {
        UUID userId = requireCurrentUserId();
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new WorkspaceNotFoundException(workspaceId));
    }

    /**
     * Verify the current user has one of the allowed roles in the workspace.
     */
    public WorkspaceMember requireRole(UUID workspaceId, WorkspaceRole... allowedRoles) {
        WorkspaceMember member = requireMembership(workspaceId);
        Set<WorkspaceRole> allowed = Set.of(allowedRoles);
        if (!allowed.contains(member.getRole())) {
            throw new WorkspaceAccessDeniedException(
                    "You need " + allowed + " role to perform this action");
        }
        return member;
    }

    /**
     * Get all workspace IDs the current user has access to.
     */
    public List<UUID> getAccessibleWorkspaceIds() {
        UUID userId = requireCurrentUserId();
        return workspaceRepository.findWorkspaceIdsByMembersUserId(userId);
    }
}
