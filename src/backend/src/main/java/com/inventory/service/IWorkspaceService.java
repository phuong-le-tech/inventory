package com.inventory.service;

import com.inventory.dto.request.InviteRequest;
import com.inventory.dto.request.UpdateMemberRoleRequest;
import com.inventory.dto.request.WorkspaceRequest;
import com.inventory.model.Workspace;
import com.inventory.model.WorkspaceInvitation;
import com.inventory.model.WorkspaceMember;

import com.inventory.enums.WorkspaceRole;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IWorkspaceService {

    List<Workspace> getMyWorkspaces();

    Workspace getWorkspaceById(UUID id);

    Workspace createWorkspace(WorkspaceRequest request);

    Workspace updateWorkspace(UUID id, WorkspaceRequest request);

    void deleteWorkspace(UUID id);

    List<WorkspaceMember> getMembers(UUID workspaceId);

    void inviteMember(UUID workspaceId, InviteRequest request);

    void acceptInvitation(String token);

    void declineInvitation(String token);

    void acceptInvitationById(UUID invitationId);

    void declineInvitationById(UUID invitationId);

    void removeMember(UUID workspaceId, UUID userId);

    void updateMemberRole(UUID workspaceId, UUID userId, UpdateMemberRoleRequest request);

    List<WorkspaceInvitation> getPendingInvitations();

    Workspace createDefaultWorkspace(com.inventory.model.User user);

    Map<UUID, Long> batchCountListsByWorkspaceIds(List<UUID> workspaceIds);

    Map<UUID, Long> batchCountMembersByWorkspaceIds(List<UUID> workspaceIds);

    Map<UUID, WorkspaceRole> batchFetchRoles(List<UUID> workspaceIds, UUID userId);

    WorkspaceRole findUserRole(UUID workspaceId, UUID userId);

    long countListsByWorkspaceId(UUID workspaceId);

    long countMembersByWorkspaceId(UUID workspaceId);
}
