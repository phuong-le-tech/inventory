package com.inventory.repository;

import com.inventory.model.Workspace;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE m.user.id = :userId ORDER BY w.isDefault DESC, w.name ASC")
    List<Workspace> findByMembersUserId(@Param("userId") UUID userId);

    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE w.id = :id AND m.user.id = :userId")
    Optional<Workspace> findByIdAndMembersUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    long countByOwnerId(UUID ownerId);

    Optional<Workspace> findByOwnerIdAndIsDefaultTrue(UUID ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Workspace w WHERE w.id = :id")
    Optional<Workspace> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT w.id FROM Workspace w JOIN w.members m WHERE m.user.id = :userId")
    List<UUID> findWorkspaceIdsByMembersUserId(@Param("userId") UUID userId);
}
