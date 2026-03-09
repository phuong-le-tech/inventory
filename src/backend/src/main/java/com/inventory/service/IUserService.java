package com.inventory.service;

import com.inventory.dto.request.CreateUserRequest;
import com.inventory.dto.response.AdminDashboardStats;
import com.inventory.dto.response.AdminUserDetailResponse;
import com.inventory.enums.Role;
import com.inventory.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.UUID;

public interface IUserService {

    Page<User> getAllUsers(@NonNull Pageable pageable);

    Page<User> searchUsers(@Nullable String search, @Nullable Role role, @Nullable Boolean enabled, @NonNull Pageable pageable);

    AdminUserDetailResponse getUserDetail(@NonNull UUID id);

    User createUser(@NonNull CreateUserRequest request);

    void deleteUser(@NonNull UUID id);

    User updateUserRole(@NonNull UUID id, @NonNull Role role);

    User updateUserStatus(@NonNull UUID id, boolean enabled);

    void triggerPasswordReset(@NonNull UUID id);

    AdminDashboardStats getAdminDashboardStats();
}
