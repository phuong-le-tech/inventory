package com.inventory.repository;

import com.inventory.enums.InvitationStatus;
import com.inventory.model.WorkspaceInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, UUID> {

    Optional<WorkspaceInvitation> findByToken(String token);

    List<WorkspaceInvitation> findByWorkspaceIdAndStatus(UUID workspaceId, InvitationStatus status);

    List<WorkspaceInvitation> findByEmailAndStatus(String email, InvitationStatus status);

    boolean existsByWorkspaceIdAndEmailAndStatus(UUID workspaceId, String email, InvitationStatus status);

    void deleteByWorkspaceIdAndEmailAndStatusNot(UUID workspaceId, String email, InvitationStatus status);
}
