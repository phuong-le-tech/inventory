package com.inventory.service;

import com.inventory.dto.request.GoogleAuthRequest;
import com.inventory.dto.request.LoginRequest;
import com.inventory.dto.request.SignupRequest;
import com.inventory.dto.response.AuthResponse;
import com.inventory.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;

import java.util.UUID;

public interface IAuthService {

    AuthResponse login(@NonNull LoginRequest request, @NonNull HttpServletResponse response);

    AuthResponse signup(@NonNull SignupRequest request, @NonNull HttpServletResponse response);

    AuthResponse googleAuth(@NonNull GoogleAuthRequest request, @NonNull HttpServletResponse response);

    void verifyEmail(@NonNull String token);

    void resendVerification(@NonNull String email);

    void forgotPassword(@NonNull String email);

    void resetPassword(@NonNull String token, @NonNull String newPassword);

    void logout(@NonNull HttpServletResponse response);

    UserResponse getCurrentUser(@NonNull String email);

    void deleteAccount(@NonNull UUID userId, @NonNull HttpServletResponse response);
}
