package com.inventory.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.inventory.dto.request.CreateUserRequest;
import com.inventory.dto.request.GoogleAuthRequest;
import com.inventory.dto.request.LoginRequest;
import com.inventory.dto.request.SignupRequest;
import com.inventory.dto.response.AuthResponse;
import com.inventory.dto.response.UserResponse;
import com.inventory.enums.Role;
import com.inventory.enums.TokenType;
import com.inventory.exception.AccountNotVerifiedException;
import com.inventory.exception.UnauthorizedException;
import com.inventory.exception.UserNotFoundException;
import com.inventory.model.User;
import com.inventory.model.VerificationToken;
import com.inventory.repository.UserRepository;
import com.inventory.repository.VerificationTokenRepository;
import com.inventory.security.CookieService;
import com.inventory.security.CustomUserDetails;
import com.inventory.security.JwtService;
import com.inventory.service.EmailSender;
import com.inventory.service.IAuthService;
import com.inventory.service.IUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final UserRepository userRepository;
    private final IUserService userService;
    private final RestTemplate restTemplate;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final int VERIFICATION_TOKEN_EXPIRY_HOURS = 24;
    private static final int PASSWORD_RESET_TOKEN_EXPIRY_HOURS = 1;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail())
            .orElseThrow(() -> new UserNotFoundException(userDetails.getEmail()));

        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Account not verified. Please check your email.");
        }

        setAuthCookie(response, user);

        return new AuthResponse(
            UserResponse.fromEntity(user),
            "Login successful"
        );
    }

    @Override
    public AuthResponse signup(SignupRequest request, HttpServletResponse response) {
        CreateUserRequest createRequest = new CreateUserRequest(
            request.email(),
            request.password(),
            Role.USER
        );

        User user = userService.createUser(createRequest);
        user.setEnabled(false);
        user = userRepository.save(user);

        sendVerificationEmail(user);

        return new AuthResponse(
            UserResponse.fromEntity(user),
            "Signup successful. Please check your email to verify your account."
        );
    }

    @Override
    public AuthResponse googleAuth(GoogleAuthRequest request, HttpServletResponse response) {
        JsonNode userInfo = fetchGoogleUserInfo(request.credential());

        String googleId = userInfo.get("sub").asText();
        String email = userInfo.get("email").asText();
        String pictureUrl = userInfo.has("picture") ? userInfo.get("picture").asText() : null;

        User user = userRepository.findByGoogleId(googleId)
            .orElseGet(() -> findOrCreateGoogleUser(googleId, email, pictureUrl));

        setAuthCookie(response, user);

        return new AuthResponse(
            UserResponse.fromEntity(user),
            "Google authentication successful"
        );
    }

    @Override
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository
            .findByTokenAndType(token, TokenType.EMAIL_VERIFICATION)
            .orElseThrow(() -> new UnauthorizedException("Invalid verification token"));

        // Delete token first to prevent replay attacks
        verificationTokenRepository.delete(verificationToken);

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void resendVerification(String email) {
        userRepository.findByEmail(email)
            .filter(user -> !user.isEnabled())
            .ifPresent(this::sendVerificationEmail);
        // Always return success to prevent email enumeration
    }

    @Override
    public void forgotPassword(String email) {
        userRepository.findByEmail(email)
            .filter(User::isEnabled)
            .ifPresent(this::sendPasswordResetEmail);
        // Always return success to prevent email enumeration
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        VerificationToken resetToken = verificationTokenRepository
            .findByTokenAndType(token, TokenType.PASSWORD_RESET)
            .orElseThrow(() -> new UnauthorizedException("Invalid reset token"));

        // Delete token first to prevent replay attacks
        verificationTokenRepository.delete(resetToken);

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void logout(HttpServletResponse response) {
        cookieService.clearAccessTokenCookie(response);
    }

    @Override
    public void deleteAccount(UUID userId, HttpServletResponse response) {
        userService.deleteUser(userId);
        cookieService.clearAccessTokenCookie(response);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));

        return UserResponse.fromEntity(user);
    }

    private void sendVerificationEmail(User user) {
        verificationTokenRepository.deleteByUserAndType(user, TokenType.EMAIL_VERIFICATION);

        VerificationToken token = new VerificationToken();
        token.setToken(generateSecureToken());
        token.setUser(user);
        token.setType(TokenType.EMAIL_VERIFICATION);
        token.setExpiresAt(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_EXPIRY_HOURS));
        verificationTokenRepository.save(token);

        String verifyUrl = frontendUrl + "/verify-email?token=" + token.getToken();
        String html = """
            <div style="font-family: sans-serif; max-width: 600px; margin: 0 auto;">
                <h2>Verifiez votre adresse email</h2>
                <p>Merci de vous etre inscrit sur Inventory. Cliquez sur le lien ci-dessous pour verifier votre compte :</p>
                <p><a href="%s" style="display: inline-block; padding: 12px 24px; background-color: #171717; color: #ffffff; text-decoration: none; border-radius: 8px;">Verifier mon email</a></p>
                <p style="color: #666; font-size: 14px;">Ce lien expire dans %d heures.</p>
                <p style="color: #666; font-size: 14px;">Si vous n'avez pas cree de compte, vous pouvez ignorer cet email.</p>
            </div>
            """.formatted(verifyUrl, VERIFICATION_TOKEN_EXPIRY_HOURS);

        emailSender.send(user.getEmail(), "Verifiez votre adresse email - Inventory", html);
    }

    private void sendPasswordResetEmail(User user) {
        verificationTokenRepository.deleteByUserAndType(user, TokenType.PASSWORD_RESET);

        VerificationToken token = new VerificationToken();
        token.setToken(generateSecureToken());
        token.setUser(user);
        token.setType(TokenType.PASSWORD_RESET);
        token.setExpiresAt(LocalDateTime.now().plusHours(PASSWORD_RESET_TOKEN_EXPIRY_HOURS));
        verificationTokenRepository.save(token);

        String resetUrl = frontendUrl + "/reset-password?token=" + token.getToken();
        String html = """
            <div style="font-family: sans-serif; max-width: 600px; margin: 0 auto;">
                <h2>Reinitialiser votre mot de passe</h2>
                <p>Vous avez demande la reinitialisation de votre mot de passe. Cliquez sur le lien ci-dessous :</p>
                <p><a href="%s" style="display: inline-block; padding: 12px 24px; background-color: #171717; color: #ffffff; text-decoration: none; border-radius: 8px;">Reinitialiser mon mot de passe</a></p>
                <p style="color: #666; font-size: 14px;">Ce lien expire dans %d heure(s).</p>
                <p style="color: #666; font-size: 14px;">Si vous n'avez pas fait cette demande, vous pouvez ignorer cet email.</p>
            </div>
            """.formatted(resetUrl, PASSWORD_RESET_TOKEN_EXPIRY_HOURS);

        emailSender.send(user.getEmail(), "Reinitialisation du mot de passe - Inventory", html);
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private void setAuthCookie(HttpServletResponse response, User user) {
        String token = jwtService.generateToken(
            user.getId(),
            user.getEmail(),
            user.getRole().name()
        );
        cookieService.setAccessTokenCookie(response, token);
    }

    private JsonNode fetchGoogleUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                GOOGLE_USERINFO_URL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
            );

            JsonNode body = response.getBody();
            if (body == null || !body.has("sub") || !body.has("email")) {
                throw new UnauthorizedException("Invalid Google user info response");
            }

            return body;
        } catch (RestClientException e) {
            log.warn("Failed to verify Google access token: {}", e.getMessage());
            throw new UnauthorizedException("Invalid Google credential");
        }
    }

    private User findOrCreateGoogleUser(String googleId, String email, String pictureUrl) {
        return userRepository.findByEmail(email)
            .map(existingUser -> {
                existingUser.setGoogleId(googleId);
                existingUser.setPictureUrl(pictureUrl);
                existingUser.setEnabled(true);
                return userRepository.save(existingUser);
            })
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setGoogleId(googleId);
                newUser.setPictureUrl(pictureUrl);
                newUser.setRole(Role.USER);
                newUser.setEnabled(true);
                return userRepository.save(newUser);
            });
    }
}
