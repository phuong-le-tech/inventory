package com.inventory.service;

import com.inventory.dto.request.GoogleAuthRequest;
import com.inventory.dto.request.LoginRequest;
import com.inventory.dto.request.SignupRequest;
import com.inventory.dto.response.AuthResponse;
import com.inventory.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

public interface IAuthService {

    AuthResponse login(LoginRequest request, HttpServletResponse response);

    AuthResponse signup(SignupRequest request, HttpServletResponse response);

    AuthResponse googleAuth(GoogleAuthRequest request, HttpServletResponse response);

    void verifyEmail(String token);

    void resendVerification(String email);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    void logout(HttpServletResponse response);

    UserResponse getCurrentUser(String email);

    void deleteAccount(UUID userId, HttpServletResponse response);
}
