package com.inventory.service.impl;

import com.inventory.dto.request.CreateUserRequest;
import com.inventory.enums.Role;
import com.inventory.exception.UserAlreadyExistsException;
import com.inventory.exception.UserNotFoundException;
import com.inventory.model.User;
import com.inventory.repository.UserRepository;
import com.inventory.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(@NonNull Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User createUser(@NonNull CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role() != null ? request.role() : Role.USER);

        return userRepository.save(user);
    }

    // SERIALIZABLE isolation + findByIdWithLock prevents TOCTOU on last-admin check
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteUser(@NonNull UUID id) {
        User user = userRepository.findByIdWithLock(id)
            .orElseThrow(() -> new UserNotFoundException(id));

        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                log.warn("Attempted to delete last admin user: {}", user.getEmail());
                throw new IllegalStateException("Cannot delete the last admin user");
            }
        }

        userRepository.deleteById(id);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public User updateUserRole(@NonNull UUID id, @NonNull Role role) {
        User user = userRepository.findByIdWithLock(id)
            .orElseThrow(() -> new UserNotFoundException(id));

        if (user.getRole() == Role.ADMIN && role != Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                log.warn("Attempted to remove last admin user: {}", user.getEmail());
                throw new IllegalStateException("Cannot remove the last admin user");
            }
        }

        user.setRole(role);
        return userRepository.save(user);
    }

}
