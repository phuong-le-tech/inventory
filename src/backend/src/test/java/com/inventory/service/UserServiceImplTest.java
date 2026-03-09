package com.inventory.service;

import com.inventory.dto.request.CreateUserRequest;
import com.inventory.dto.response.AdminDashboardStats;
import com.inventory.dto.response.AdminUserDetailResponse;
import com.inventory.enums.Role;
import com.inventory.exception.UserAlreadyExistsException;
import com.inventory.exception.UserNotFoundException;
import com.inventory.model.User;
import com.inventory.repository.ItemListRepository;
import com.inventory.repository.ItemRepository;
import com.inventory.repository.UserRepository;
import com.inventory.repository.VerificationTokenRepository;
import com.inventory.service.EmailSender;
import com.inventory.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemListRepository itemListRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailSender emailSender;

    @Mock
    private Executor emailExecutor;

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

    @Nested
    @DisplayName("getUserDetail")
    class GetUserDetail {

        @Test
        @DisplayName("returns user detail with counts")
        void returnsUserDetailWithCounts() {
            UUID id = UUID.randomUUID();
            User user = createUser(id, "user@test.com", Role.USER);
            user.setEnabled(true);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(itemListRepository.countByUserId(id)).thenReturn(3L);
            when(itemRepository.countByUserId(id)).thenReturn(12L);

            AdminUserDetailResponse result = userService.getUserDetail(id);

            assertThat(result.id()).isEqualTo(id);
            assertThat(result.email()).isEqualTo("user@test.com");
            assertThat(result.role()).isEqualTo(Role.USER);
            assertThat(result.listCount()).isEqualTo(3L);
            assertThat(result.itemCount()).isEqualTo(12L);
            assertThat(result.enabled()).isTrue();
        }

        @Test
        @DisplayName("throws when user not found")
        void throwsWhenUserNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserDetail(id))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateUserStatus")
    class UpdateUserStatus {

        @Test
        @DisplayName("disables user")
        void disablesUser() {
            UUID id = UUID.randomUUID();
            User user = createUser(id, "user@test.com", Role.USER);
            user.setEnabled(true);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.updateUserStatus(id, false);

            assertThat(result.isEnabled()).isFalse();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("enables user")
        void enablesUser() {
            UUID id = UUID.randomUUID();
            User user = createUser(id, "user@test.com", Role.USER);
            user.setEnabled(false);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.updateUserStatus(id, true);

            assertThat(result.isEnabled()).isTrue();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("throws when user not found")
        void throwsWhenUserNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUserStatus(id, false))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("triggerPasswordReset")
    class TriggerPasswordReset {

        @Test
        @DisplayName("creates token in transaction and sends email async")
        void triggersResetForExistingUser() {
            UUID id = UUID.randomUUID();
            User user = createUser(id, "user@test.com", Role.USER);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(verificationTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            // Make executor run synchronously for testing
            doAnswer(inv -> { ((Runnable) inv.getArgument(0)).run(); return null; })
                .when(emailExecutor).execute(any(Runnable.class));

            userService.triggerPasswordReset(id);

            // Token operations happen synchronously (in transaction)
            verify(verificationTokenRepository).deleteByUserAndType(eq(user), eq(com.inventory.enums.TokenType.PASSWORD_RESET));
            verify(verificationTokenRepository).save(any());
            // Email sent via async executor
            verify(emailSender).send(eq("user@test.com"), any(), any());
        }

        @Test
        @DisplayName("throws when user not found")
        void throwsWhenUserNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.triggerPasswordReset(id))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @SuppressWarnings("unchecked")
    @Nested
    @DisplayName("getAdminDashboardStats")
    class GetAdminDashboardStats {

        @Test
        @DisplayName("returns aggregated dashboard stats")
        void returnsAggregatedStats() {
            when(userRepository.count()).thenReturn(100L);
            when(userRepository.countByEnabled(true)).thenReturn(85L);
            when(userRepository.countByRole(Role.PREMIUM_USER)).thenReturn(10L);
            when(userRepository.countByRole(Role.ADMIN)).thenReturn(2L);
            when(userRepository.countUsersByMonth(any())).thenReturn(
                List.<Object[]>of(new Object[]{2026, 1, 5L}, new Object[]{2026, 2, 8L})
            );
            when(userRepository.findTopUsersByListCount()).thenReturn(
                List.<Object[]>of(new Object[]{"top@test.com", 10L})
            );
            when(itemRepository.findTopUsersByItemCount()).thenReturn(
                List.<Object[]>of(new Object[]{"items@test.com", 25L})
            );

            AdminDashboardStats stats = userService.getAdminDashboardStats();

            assertThat(stats.totalUsers()).isEqualTo(100L);
            assertThat(stats.activeUsers()).isEqualTo(85L);
            assertThat(stats.premiumUsers()).isEqualTo(10L);
            assertThat(stats.adminUsers()).isEqualTo(2L);
            assertThat(stats.premiumConversionRate()).isEqualTo(10.0);
            assertThat(stats.registrationTrend()).containsEntry("2026-01", 5L).containsEntry("2026-02", 8L);
            assertThat(stats.topUsersByLists()).hasSize(1);
            assertThat(stats.topUsersByLists().getFirst().email()).isEqualTo("top@test.com");
            assertThat(stats.topUsersByItems()).hasSize(1);
            assertThat(stats.topUsersByItems().getFirst().count()).isEqualTo(25L);
        }

        @Test
        @DisplayName("handles zero users for conversion rate")
        void handlesZeroUsersForConversionRate() {
            when(userRepository.count()).thenReturn(0L);
            when(userRepository.countByEnabled(true)).thenReturn(0L);
            when(userRepository.countByRole(Role.PREMIUM_USER)).thenReturn(0L);
            when(userRepository.countByRole(Role.ADMIN)).thenReturn(0L);
            when(userRepository.countUsersByMonth(any())).thenReturn(List.of());
            when(userRepository.findTopUsersByListCount()).thenReturn(List.of());
            when(itemRepository.findTopUsersByItemCount()).thenReturn(List.of());

            AdminDashboardStats stats = userService.getAdminDashboardStats();

            assertThat(stats.premiumConversionRate()).isEqualTo(0.0);
            assertThat(stats.registrationTrend()).isEmpty();
            assertThat(stats.topUsersByLists()).isEmpty();
            assertThat(stats.topUsersByItems()).isEmpty();
        }
    }

    @SuppressWarnings("unchecked")
    @Nested
    @DisplayName("searchUsers")
    class SearchUsers {

        @Test
        @DisplayName("delegates to specification-based search")
        void delegatesToSpecificationSearch() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> expectedPage = new PageImpl<>(List.of(createUser(UUID.randomUUID(), "found@test.com", Role.USER)));
            when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

            Page<User> result = userService.searchUsers("found", Role.USER, true, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getEmail()).isEqualTo("found@test.com");
            verify(userRepository).findAll(any(Specification.class), eq(pageable));
        }
    }
}
