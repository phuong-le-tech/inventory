package com.inventory.repository;

import com.inventory.model.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    boolean existsByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    List<WorkspaceMember> findByWorkspaceId(UUID workspaceId);

    void deleteByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    List<WorkspaceMember> findByWorkspaceIdInAndUserId(List<UUID> workspaceIds, UUID userId);

    long countByWorkspaceId(UUID workspaceId);

    @org.springframework.data.jpa.repository.Query("SELECT m.workspace.id, COUNT(m) FROM WorkspaceMember m WHERE m.workspace.id IN :workspaceIds GROUP BY m.workspace.id")
    List<Object[]> countMembersByWorkspaceIds(@org.springframework.data.repository.query.Param("workspaceIds") List<UUID> workspaceIds);
}
