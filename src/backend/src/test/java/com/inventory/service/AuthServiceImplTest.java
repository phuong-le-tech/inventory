package com.inventory.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.inventory.dto.request.CreateUserRequest;
import com.inventory.dto.request.GoogleAuthRequest;
import com.inventory.dto.request.LoginRequest;
import com.inventory.dto.request.SignupRequest;
import com.inventory.dto.response.AuthResponse;
import com.inventory.enums.Role;
import com.inventory.exception.AccountNotVerifiedException;
import com.inventory.exception.UnauthorizedException;
import com.inventory.exception.UserAlreadyExistsException;
import com.inventory.exception.UserNotFoundException;
import com.inventory.model.User;
import com.inventory.repository.UserRepository;
import com.inventory.repository.VerificationTokenRepository;
import com.inventory.security.CookieService;
import com.inventory.security.CustomUserDetails;
import com.inventory.security.JwtService;
import com.inventory.service.impl.AuthServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private CookieService cookieService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IUserService userService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private EmailSender emailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletResponse httpResponse;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://localhost:5173");

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded-password");
        testUser.setRole(Role.USER);
        testUser.setEnabled(true);
    }

    @Nested
    @DisplayName("signup")
    class SignupTests {

        @Test
        @DisplayName("should create user with USER role, disable account, and send verification email")
        void signup_success() {
            SignupRequest request = new SignupRequest("new@example.com", "password123", "password123");

            when(userService.createUser(any(CreateUserRequest.class))).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(verificationTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AuthResponse response = authService.signup(request, httpResponse);

            assertThat(response.user().email()).isEqualTo(testUser.getEmail());
            assertThat(response.message()).contains("verify");

            // Verify user was created with USER role
            ArgumentCaptor<CreateUserRequest> captor = ArgumentCaptor.forClass(CreateUserRequest.class);
            verify(userService).createUser(captor.capture());
            assertThat(captor.getValue().role()).isEqualTo(Role.USER);
            assertThat(captor.getValue().email()).isEqualTo("new@example.com");

            // Verify no JWT cookie was set (user must verify email first)
            verify(cookieService, never()).setAccessTokenCookie(any(), anyString());

            // Verify verification email was sent
            verify(emailSender).send(eq(testUser.getEmail()), anyString(), anyString());
        }

        @Test
        @DisplayName("should throw UserAlreadyExistsException for duplicate email")
        void signup_duplicateEmail() {
            SignupRequest request = new SignupRequest("existing@example.com", "password123", "password123");

            when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("existing@example.com"));

            assertThatThrownBy(() -> authService.signup(request, httpResponse))
                .isInstanceOf(UserAlreadyExistsException.class);

            verify(cookieService, never()).setAccessTokenCookie(any(), anyString());
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        @DisplayName("should authenticate and set JWT cookie")
        void login_success() {
            LoginRequest request = new LoginRequest("test@example.com", "password123");
            CustomUserDetails userDetails = new CustomUserDetails(
                testUser.getId(), testUser.getEmail(), testUser.getRole().name()
            );
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);

            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(any(UUID.class), anyString(), anyString())).thenReturn("jwt-token");

            AuthResponse response = authService.login(request, httpResponse);

            assertThat(response.user().email()).isEqualTo("test@example.com");
            assertThat(response.message()).isEqualTo("Login successful");
            verify(cookieService).setAccessTokenCookie(httpResponse, "jwt-token");
        }

        @Test
        @DisplayName("should throw AccountNotVerifiedException for unverified account")
        void login_unverifiedAccount() {
            testUser.setEnabled(false);
            LoginRequest request = new LoginRequest("test@example.com", "password123");
            CustomUserDetails userDetails = new CustomUserDetails(
                testUser.getId(), testUser.getEmail(), testUser.getRole().name()
            );
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);

            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> authService.login(request, httpResponse))
                .isInstanceOf(AccountNotVerifiedException.class)
                .hasMessageContaining("not verified");

            verify(cookieService, never()).setAccessTokenCookie(any(), anyString());
        }
    }

    @Nested
    @DisplayName("getCurrentUser")
    class GetCurrentUserTests {

        @Test
        @DisplayName("should return user by email")
        void getCurrentUser_success() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            var response = authService.getCurrentUser("test@example.com");

            assertThat(response.email()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("should throw UserNotFoundException for unknown email")
        void getCurrentUser_notFound() {
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.getCurrentUser("unknown@example.com"))
                .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("logout")
    class LogoutTests {

        @Test
        @DisplayName("should clear access token cookie")
        void logout_success() {
            authService.logout(httpResponse);

            verify(cookieService).clearAccessTokenCookie(httpResponse);
        }
    }

    @Nested
    @DisplayName("googleAuth")
    class GoogleAuthTests {

        private static final ObjectMapper mapper = new ObjectMapper();

        private ObjectNode validUserInfo() {
            ObjectNode node = mapper.createObjectNode();
            node.put("sub", "google-id-123");
            node.put("email", "google@example.com");
            node.put("picture", "https://lh3.googleusercontent.com/photo.jpg");
            return node;
        }

        private void mockGoogleUserInfoResponse(JsonNode body) {
            when(restTemplate.exchange(
                eq("https://www.googleapis.com/oauth2/v3/userinfo"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(JsonNode.class)
            )).thenReturn(ResponseEntity.ok(body));
        }

        @Test
        @DisplayName("should authenticate existing Google user")
        void googleAuth_existingGoogleUser() {
            mockGoogleUserInfoResponse(validUserInfo());

            User googleUser = new User();
            googleUser.setId(UUID.randomUUID());
            googleUser.setEmail("google@example.com");
            googleUser.setGoogleId("google-id-123");
            googleUser.setRole(Role.USER);
            when(userRepository.findByGoogleId("google-id-123")).thenReturn(Optional.of(googleUser));
            when(jwtService.generateToken(any(UUID.class), anyString(), anyString())).thenReturn("jwt-token");

            AuthResponse response = authService.googleAuth(new GoogleAuthRequest("valid-token"), httpResponse);

            assertThat(response.user().email()).isEqualTo("google@example.com");
            assertThat(response.message()).isEqualTo("Google authentication successful");
            verify(cookieService).setAccessTokenCookie(httpResponse, "jwt-token");
            verify(userRepository, never()).findByEmail(anyString());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should link Google account to existing email user")
        void googleAuth_linkToExistingEmailUser() {
            mockGoogleUserInfoResponse(validUserInfo());

            User existingUser = new User();
            existingUser.setId(UUID.randomUUID());
            existingUser.setEmail("google@example.com");
            existingUser.setRole(Role.USER);
            when(userRepository.findByGoogleId("google-id-123")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateToken(any(UUID.class), anyString(), anyString())).thenReturn("jwt-token");

            AuthResponse response = authService.googleAuth(new GoogleAuthRequest("valid-token"), httpResponse);

            assertThat(response.user().email()).isEqualTo("google@example.com");
            assertThat(response.user().hasGoogleAccount()).isTrue();

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getGoogleId()).isEqualTo("google-id-123");
            assertThat(captor.getValue().getPictureUrl()).isEqualTo("https://lh3.googleusercontent.com/photo.jpg");
        }

        @Test
        @DisplayName("should create new user for unknown Google account")
        void googleAuth_createNewUser() {
            mockGoogleUserInfoResponse(validUserInfo());

            when(userRepository.findByGoogleId("google-id-123")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                return u;
            });
            when(jwtService.generateToken(any(UUID.class), anyString(), anyString())).thenReturn("jwt-token");

            AuthResponse response = authService.googleAuth(new GoogleAuthRequest("valid-token"), httpResponse);

            assertThat(response.user().email()).isEqualTo("google@example.com");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();
            assertThat(saved.getGoogleId()).isEqualTo("google-id-123");
            assertThat(saved.getEmail()).isEqualTo("google@example.com");
            assertThat(saved.getRole()).isEqualTo(Role.USER);
            assertThat(saved.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("should throw UnauthorizedException for invalid token")
        void googleAuth_invalidToken() {
            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(JsonNode.class)
            )).thenThrow(new RestClientException("401 Unauthorized"));

            assertThatThrownBy(() -> authService.googleAuth(new GoogleAuthRequest("bad-token"), httpResponse))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid Google credential");
        }

        @Test
        @DisplayName("should throw UnauthorizedException when userinfo missing required fields")
        void googleAuth_missingFields() {
            ObjectNode incomplete = mapper.createObjectNode();
            incomplete.put("sub", "google-id-123");
            // missing "email"
            mockGoogleUserInfoResponse(incomplete);

            assertThatThrownBy(() -> authService.googleAuth(new GoogleAuthRequest("valid-token"), httpResponse))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid Google user info response");
        }
    }
}
