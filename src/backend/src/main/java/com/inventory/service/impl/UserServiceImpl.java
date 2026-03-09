package com.inventory.service.impl;

import com.inventory.dto.request.CreateUserRequest;
import com.inventory.dto.response.AdminDashboardStats;
import com.inventory.dto.response.AdminUserDetailResponse;
import com.inventory.enums.Role;
import com.inventory.enums.TokenType;
import com.inventory.exception.UserAlreadyExistsException;
import com.inventory.exception.UserNotFoundException;
import com.inventory.model.User;
import com.inventory.model.VerificationToken;
import com.inventory.repository.ItemListRepository;
import com.inventory.repository.ItemRepository;
import com.inventory.repository.UserRepository;
import com.inventory.repository.VerificationTokenRepository;
import com.inventory.repository.specification.UserSpecification;
import com.inventory.service.EmailSender;
import com.inventory.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final ItemListRepository itemListRepository;
    private final ItemRepository itemRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final Executor emailExecutor;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final int PASSWORD_RESET_TOKEN_EXPIRY_MINUTES = 15;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(@NonNull Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsers(@Nullable String search, @Nullable Role role, @Nullable Boolean enabled, @NonNull Pageable pageable) {
        return userRepository.findAll(UserSpecification.withCriteria(search, role, enabled), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(@NonNull UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));

        long listCount = itemListRepository.countByUserId(id);
        long itemCount = itemRepository.countByUserId(id);

        return AdminUserDetailResponse.fromEntity(user, listCount, itemCount);
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

    @Override
    public User updateUserStatus(@NonNull UUID id, boolean enabled) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));

        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    @Override
    public void triggerPasswordReset(@NonNull UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));

        // Token operations stay in the parent @Transactional
        verificationTokenRepository.deleteByUserAndType(user, TokenType.PASSWORD_RESET);

        VerificationToken token = new VerificationToken();
        token.setToken(generateSecureToken());
        token.setUser(user);
        token.setType(TokenType.PASSWORD_RESET);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(PASSWORD_RESET_TOKEN_EXPIRY_MINUTES));
        verificationTokenRepository.save(token);

        String resetUrl = escapeHtml(frontendUrl + "/reset-password?token=" + token.getToken());
        String email = user.getEmail();
        UUID userId = user.getId();

        // Only email sending is async (fire-and-forget)
        CompletableFuture.runAsync(() -> {
            try {
                sendPasswordResetEmail(email, resetUrl);
            } catch (Exception e) {
                log.error("Failed to send password reset email to user {}: {}", userId, e.getMessage(), e);
            }
        }, emailExecutor);
    }

    private void sendPasswordResetEmail(String email, String resetUrl) {
        String html = """
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8"></head><body>
            <div style="font-family: sans-serif; max-width: 600px; margin: 0 auto;">
                <h2>Reinitialiser votre mot de passe</h2>
                <p>Un administrateur a demande la reinitialisation de votre mot de passe. Cliquez sur le lien ci-dessous :</p>
                <p><a href="%s" style="display: inline-block; padding: 12px 24px; background-color: #171717; color: #ffffff; text-decoration: none; border-radius: 8px;">Reinitialiser mon mot de passe</a></p>
                <p style="color: #666; font-size: 14px;">Ce lien expire dans %d minutes.</p>
                <p style="color: #666; font-size: 14px;">Si vous n'avez pas fait cette demande, vous pouvez ignorer cet email.</p>
            </div>
            </body></html>
            """.formatted(resetUrl, PASSWORD_RESET_TOKEN_EXPIRY_MINUTES);

        emailSender.send(email, "Reinitialisation du mot de passe - Inventory", html);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStats getAdminDashboardStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByEnabled(true);
        long premiumUsers = userRepository.countByRole(Role.PREMIUM_USER);
        long adminUsers = userRepository.countByRole(Role.ADMIN);
        double premiumConversionRate = totalUsers > 0
            ? (premiumUsers / (double) totalUsers) * 100
            : 0.0;

        return new AdminDashboardStats(
            totalUsers, activeUsers, premiumUsers, adminUsers,
            premiumConversionRate, buildRegistrationTrend(),
            buildTopUsersByLists(), buildTopUsersByItems()
        );
    }

    private Map<String, Long> buildRegistrationTrend() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6)
            .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return userRepository.countUsersByMonth(sixMonthsAgo).stream()
            .collect(Collectors.toMap(
                row -> String.format("%d-%02d", ((Number) row[0]).intValue(), ((Number) row[1]).intValue()),
                row -> ((Number) row[2]).longValue(),
                (a, b) -> a,
                LinkedHashMap::new
            ));
    }

    private List<AdminDashboardStats.UserStat> buildTopUsersByLists() {
        return userRepository.findTopUsersByListCount().stream()
            .map(row -> new AdminDashboardStats.UserStat((String) row[0], (Long) row[1]))
            .toList();
    }

    private List<AdminDashboardStats.UserStat> buildTopUsersByItems() {
        return itemRepository.findTopUsersByItemCount().stream()
            .map(row -> new AdminDashboardStats.UserStat((String) row[0], (Long) row[1]))
            .toList();
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private static String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}
