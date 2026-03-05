package com.inventory.service;

import com.inventory.dto.request.CreateUserRequest;
import com.inventory.enums.Role;
import com.inventory.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.UUID;

public interface IUserService {

    Page<User> getAllUsers(@NonNull Pageable pageable);

    User createUser(@NonNull CreateUserRequest request);

    void deleteUser(@NonNull UUID id);

    User updateUserRole(@NonNull UUID id, @NonNull Role role);
}
