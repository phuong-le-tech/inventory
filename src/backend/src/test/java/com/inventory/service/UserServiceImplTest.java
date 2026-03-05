package com.inventory.service;

import com.inventory.dto.request.CreateUserRequest;
import com.inventory.enums.Role;
import com.inventory.exception.UserAlreadyExistsException;
import com.inventory.exception.UserNotFoundException;
import com.inventory.model.User;
import com.inventory.repository.UserRepository;
import com.inventory.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User createUser(UUID id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("deletes non-admin user successfully")
        void deletesNonAdminUser() {
            UUID id = UUID.randomUUID();
            User user = createUser(id, "user@test.com", Role.USER);
            when(userRepository.findByIdWithLock(id)).thenReturn(Optional.of(user));

            userService.deleteUser(id);

            verify(userRepository).deleteById(id);
        }

        @Test
        @DisplayName("deletes admin when multiple admins exist")
        void deletesAdminWhenMultipleAdminsExist() {
            UUID id = UUID.randomUUID();
            User admin = createUser(id, "admin@test.com", Role.ADMIN);
            when(userRepository.findByIdWithLock(id)).thenReturn(Optional.of(admin));
            when(userRepository.countByRole(Role.ADMIN)).thenReturn(2L);

            userService.deleteUser(id);

            verify(userRepository).deleteById(id);
        }

        @Test
        @DisplayName("throws when deleting last admin")
        void throwsWhenDeletingLastAdmin() {
            UUID id = UUID.randomUUID();
            User admin = createUser(id, "admin@test.com", Role.ADMIN);
            when(userRepository.findByIdWithLock(id)).thenReturn(Optional.of(admin));
            when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

            assertThatThrownBy(() -> userService.deleteUser(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot delete the last admin user");

            verify(userRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("throws when user not found")
        void throwsWhenUserNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findByIdWithLock(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUser(id))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateUserRole")
    class UpdateUserRole {

        @Test
        @DisplayName("promotes user to admin")
        void promotesUserToAdmin() {
            UUID id = UUID.randomUUID();
            User user = createUser(id, "user@test.com", Role.USER);
            when(userRepository.findByIdWithLock(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.updateUserRole(id, Role.ADMIN);

            assertThat(result.getRole()).isEqualTo(Role.ADMIN);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("demotes admin when multiple admins exist")
        void demotesAdminWhenMultipleAdminsExist() {
            UUID id = UUID.randomUUID();
            User admin = createUser(id, "admin@test.com", Role.ADMIN);
            when(userRepository.findByIdWithLock(id)).thenReturn(Optional.of(admin));
            when(userRepository.countByRole(Role.ADMIN)).thenReturn(2L);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.updateUserRole(id, Role.USER);

            assertThat(result.getRole()).isEqualTo(Role.USER);
            verify(userRepository).save(admin);
        }

        @Test
        @DisplayName("throws when demoting last admin")
        void throwsWhenDemotingLastAdmin() {
            UUID id = UUID.randomUUID();
            User admin = createUser(id, "admin@test.com", Role.ADMIN);
            when(userRepository.findByIdWithLock(id)).thenReturn(Optional.of(admin));
            when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

            assertThatThrownBy(() -> userService.updateUserRole(id, Role.USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot remove the last admin user");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("allows setting admin to admin (no-op role change)")
        void allowsSettingAdminToAdmin() {
            UUID id = UUID.randomUUID();
            User admin = createUser(id, "admin@test.com", Role.ADMIN);
            when(userRepository.findByIdWithLock(id)).thenReturn(Optional.of(admin));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.updateUserRole(id, Role.ADMIN);

            assertThat(result.getRole()).isEqualTo(Role.ADMIN);
            verify(userRepository, never()).countByRole(any());
        }

        @Test
        @DisplayName("throws when user not found")
        void throwsWhenUserNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findByIdWithLock(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUserRole(id, Role.ADMIN))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("creates user successfully")
        void createsUser() {
            CreateUserRequest request = new CreateUserRequest("new@test.com", "Password123!@#", Role.USER);
            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.createUser(request);

            assertThat(result.getEmail()).isEqualTo("new@test.com");
            assertThat(result.getRole()).isEqualTo(Role.USER);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("throws when email already exists")
        void throwsWhenEmailExists() {
            CreateUserRequest request = new CreateUserRequest("existing@test.com", "Password123!@#", Role.USER);
            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
        }
    }
}
