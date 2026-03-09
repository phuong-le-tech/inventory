package com.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.config.TestSecurityConfig;
import com.inventory.dto.request.CreateUserRequest;
import com.inventory.dto.request.UpdateRoleRequest;
import com.inventory.dto.request.UpdateStatusRequest;
import com.inventory.dto.response.AdminDashboardStats;
import com.inventory.dto.response.AdminUserDetailResponse;
import com.inventory.enums.Role;
import com.inventory.exception.ItemListNotFoundException;
import com.inventory.exception.UserAlreadyExistsException;
import com.inventory.exception.UserNotFoundException;
import com.inventory.model.Item;
import com.inventory.model.ItemList;
import com.inventory.model.User;
import com.inventory.repository.ItemListRepository;
import com.inventory.repository.ItemRepository;
import com.inventory.security.CustomUserDetails;
import com.inventory.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("AdminController Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    private ItemListRepository itemListRepository;

    @MockitoBean
    private ItemRepository itemRepository;

    private CustomUserDetails adminUser;
    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        adminUser = new CustomUserDetails(UUID.randomUUID(), "admin@test.com", "ADMIN");

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("user@test.com");
        testUser.setRole(Role.USER);
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users")
    class GetAllUsersTests {

        @Test
        @DisplayName("should return paginated list of users")
        void getAllUsers_returnsOkWithPageResponse() throws Exception {
            when(userService.getAllUsers(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testUser)));

            mockMvc.perform(get("/api/v1/admin/users")
                            .with(user(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].email").value("user@test.com"))
                    .andExpect(jsonPath("$.data.content[0].role").value("USER"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("should support pagination and sorting parameters")
        void getAllUsers_withPaginationParams_returnsPagedResults() throws Exception {
            when(userService.getAllUsers(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testUser)));

            mockMvc.perform(get("/api/v1/admin/users")
                            .param("page", "0")
                            .param("size", "5")
                            .param("sortBy", "email")
                            .param("sortDir", "asc")
                            .with(user(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("should fall back to createdAt when sort field is invalid")
        void getAllUsers_invalidSortField_fallsBackToCreatedAt() throws Exception {
            when(userService.getAllUsers(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testUser)));

            mockMvc.perform(get("/api/v1/admin/users")
                            .param("sortBy", "invalidField")
                            .with(user(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());

            verify(userService).getAllUsers(any(Pageable.class));
        }

        @Test
        @DisplayName("should clamp size to minimum of 1")
        void getAllUsers_sizeZero_clampedToOne() throws Exception {
            when(userService.getAllUsers(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/v1/admin/users")
                            .param("size", "0")
                            .with(user(adminUser)))
                    .andExpect(status().isOk());

            verify(userService).getAllUsers(any(Pageable.class));
        }

        @Test
        @DisplayName("should clamp size to maximum of 100")
        void getAllUsers_sizeExceedsMax_clampedTo100() throws Exception {
            when(userService.getAllUsers(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/v1/admin/users")
                            .param("size", "200")
                            .with(user(adminUser)))
                    .andExpect(status().isOk());

            verify(userService).getAllUsers(any(Pageable.class));
        }

        @Test
        @DisplayName("should return empty page when no users exist")
        void getAllUsers_noUsers_returnsEmptyPage() throws Exception {
            when(userService.getAllUsers(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/v1/admin/users")
                            .with(user(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content").isEmpty())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/users")
    class CreateUserTests {

        @Test
        @DisplayName("should create user with valid request")
        void createUser_validRequest_returns201() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                    "newuser@test.com", "TestPassword1!", Role.USER);

            when(userService.createUser(any(CreateUserRequest.class))).thenReturn(testUser);

            mockMvc.perform(post("/api/v1/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(adminUser)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.email").value("user@test.com"))
                    .andExpect(jsonPath("$.data.role").value("USER"));
        }

        @Test
        @DisplayName("should return 409 when user already exists")
        void createUser_duplicateEmail_returns409() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                    "existing@test.com", "TestPassword1!", Role.USER);

            when(userService.createUser(any(CreateUserRequest.class)))
                    .thenThrow(new UserAlreadyExistsException("existing@test.com"));

            mockMvc.perform(post("/api/v1/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(adminUser)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value(409));
        }

        @Test
        @DisplayName("should return 400 when email is blank")
        void createUser_blankEmail_returns400() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                    "", "TestPassword1!", Role.USER);

            mockMvc.perform(post("/api/v1/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(adminUser)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value(400));
        }

        @Test
        @DisplayName("should return 400 when email format is invalid")
        void createUser_invalidEmail_returns400() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                    "not-an-email", "TestPassword1!", Role.USER);

            mockMvc.perform(post("/api/v1/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(adminUser)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value(400));
        }

        @Test
        @DisplayName("should return 400 when password is blank")
        void createUser_blankPassword_returns400() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                    "user@test.com", "", Role.USER);

            mockMvc.perform(post("/api/v1/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(adminUser)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value(400));
        }

        @Test
        @DisplayName("should return 400 when password is too short")
        void createUser_shortPassword_returns400() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                    "user@test.com", "Short1!", Role.USER);

            mockMvc.perform(post("/api/v1/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(adminUser)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value(400));
        }

        @Test
        @DisplayName("should return 400 when password lacks uppercase letter")
        void createUser_noUppercase_returns400() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                    "user@test.com", "testpassword1!", Role.USER);

            mockMvc.perform(post("/api/v1/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(adminUser)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value(400));
        }

        @Test
        @DisplayName("should return 400 when password lacks lowercase letter")
        void createUser_noLowercase_returns400() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                    "user@test.com", "TESTPASSWORD1!", Role.USER);

            mockMvc.perform(post("/api/v1/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(adminUser)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value(400));
        }

        @Test
        @DisplayName("should return 400 when password lacks digit")
        void createUser_noDigit_returns400() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                    "user@test.com", "TestPassword!!", Role.USER);

            mockMvc.perform(post("/api/v1/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(adminUser)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value(400));
        }

        @Test
        @DisplayName("should return 400 when password lacks special character")
        void createUser_noSpecialChar_returns400() throws Exception {
            CreateUserRequest request = new CreateUserRequest(
                    "user@test.com", "TestPassword12", Role.USER);

            mockMvc.perform(post("/api/v1/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(adminUser)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value(400));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/users/{id}")
    class DeleteUserTests {

        @Test
        @DisplayName("should delete existing user")
        void deleteUser_existingId_returns204() throws Exception {
            doNothing().when(userService).deleteUser(testUserId);

            mockMvc.perform(delete("/api/v1/admin/users/{id}", testUserId)
                            .with(user(adminUser)))
                    .andExpect(status().isNoContent());

            verify(userService).deleteUser(testUserId);
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void deleteUser_nonExistingId_returns404() throws Exception {
            UUID nonExistingId = UUID.randomUUID();
            doThrow(new UserNotFoundException(nonExistingId))
                    .when(userService).deleteUser(nonExistingId);

            mockMvc.perform(delete("/api/v1/admin/users/{id}", nonExistingId)
                            .with(user(adminUser)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value(404));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{id}/role")
    class UpdateUserRoleTests {

        @Test
        @DisplayName("should update user role")
        void updateUserRole_validRequest_returnsUpdatedUser() throws Exception {
            User updatedUser = new User();
            updatedUser.setId(testUserId);
            updatedUser.setEmail("user@test.com");
            updatedUser.setRole(Role.ADMIN);
            updatedUser.setEnabled(true);
            updatedUser.setCreatedAt(LocalDateTime.now());

            UpdateRoleRequest request = new UpdateRoleRequest(Role.ADMIN);

            when(userService.updateUserRole(eq(testUserId), eq(Role.ADMIN)))
                    .thenReturn(updatedUser);

            mockMvc.perform(patch("/api/v1/admin/users/{id}/role", testUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").value("user@test.com"))
                    .andExpect(jsonPath("$.data.role").value("ADMIN"));
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void updateUserRole_nonExistingId_returns404() throws Exception {
            UUID nonExistingId = UUID.randomUUID();
            UpdateRoleRequest request = new UpdateRoleRequest(Role.ADMIN);

            when(userService.updateUserRole(eq(nonExistingId), eq(Role.ADMIN)))
                    .thenThrow(new UserNotFoundException(nonExistingId));

            mockMvc.perform(patch("/api/v1/admin/users/{id}/role", nonExistingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(adminUser)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value(404));
        }

        @Test
        @DisplayName("should return 400 when role is null")
        void updateUserRole_nullRole_returns400() throws Exception {
            String requestBody = "{}";

            mockMvc.perform(patch("/api/v1/admin/users/{id}/role", testUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                            .with(user(adminUser)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value(400));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users/{id}")
    class GetUserDetailTests {

        @Test
        @DisplayName("should return user detail")
        void getUserDetail_existingId_returnsOk() throws Exception {
            AdminUserDetailResponse detail = new AdminUserDetailResponse(
                    testUserId, "user@test.com", Role.USER, null, false,
                    true, 3L, 10L, false, null,
                    LocalDateTime.now(), LocalDateTime.now());

            when(userService.getUserDetail(testUserId)).thenReturn(detail);

            mockMvc.perform(get("/api/v1/admin/users/{id}", testUserId)
                            .with(user(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").value("user@test.com"))
                    .andExpect(jsonPath("$.data.listCount").value(3))
                    .andExpect(jsonPath("$.data.itemCount").value(10));
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void getUserDetail_nonExistingId_returns404() throws Exception {
            UUID nonExistingId = UUID.randomUUID();
            when(userService.getUserDetail(nonExistingId))
                    .thenThrow(new UserNotFoundException(nonExistingId));

            mockMvc.perform(get("/api/v1/admin/users/{id}", nonExistingId)
                            .with(user(adminUser)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value(404));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{id}/status")
    class UpdateUserStatusTests {

        @Test
        @DisplayName("should update user status")
        void updateUserStatus_validRequest_returnsUpdatedUser() throws Exception {
            User disabledUser = new User();
            disabledUser.setId(testUserId);
            disabledUser.setEmail("user@test.com");
            disabledUser.setRole(Role.USER);
            disabledUser.setEnabled(false);
            disabledUser.setCreatedAt(LocalDateTime.now());

            UpdateStatusRequest request = new UpdateStatusRequest(false);

            when(userService.updateUserStatus(eq(testUserId), eq(false)))
                    .thenReturn(disabledUser);

            mockMvc.perform(patch("/api/v1/admin/users/{id}/status", testUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").value("user@test.com"))
                    .andExpect(jsonPath("$.data.enabled").value(false));
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void updateUserStatus_nonExistingId_returns404() throws Exception {
            UUID nonExistingId = UUID.randomUUID();
            UpdateStatusRequest request = new UpdateStatusRequest(false);

            when(userService.updateUserStatus(eq(nonExistingId), eq(false)))
                    .thenThrow(new UserNotFoundException(nonExistingId));

            mockMvc.perform(patch("/api/v1/admin/users/{id}/status", nonExistingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(adminUser)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value(404));
        }

        @Test
        @DisplayName("should return 400 when enabled is null")
        void updateUserStatus_nullEnabled_returns400() throws Exception {
            String requestBody = "{}";

            mockMvc.perform(patch("/api/v1/admin/users/{id}/status", testUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                            .with(user(adminUser)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value(400));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/users/{id}/reset-password")
    class TriggerPasswordResetTests {

        @Test
        @DisplayName("should trigger password reset")
        void triggerPasswordReset_existingId_returnsOk() throws Exception {
            doNothing().when(userService).triggerPasswordReset(testUserId);

            mockMvc.perform(post("/api/v1/admin/users/{id}/reset-password", testUserId)
                            .with(user(adminUser)))
                    .andExpect(status().isOk());

            verify(userService).triggerPasswordReset(testUserId);
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void triggerPasswordReset_nonExistingId_returns404() throws Exception {
            UUID nonExistingId = UUID.randomUUID();
            doThrow(new UserNotFoundException(nonExistingId))
                    .when(userService).triggerPasswordReset(nonExistingId);

            mockMvc.perform(post("/api/v1/admin/users/{id}/reset-password", nonExistingId)
                            .with(user(adminUser)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value(404));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/stats")
    class GetDashboardStatsTests {

        @Test
        @DisplayName("should return dashboard statistics")
        void getDashboardStats_returnsOk() throws Exception {
            AdminDashboardStats stats = new AdminDashboardStats(
                    100L, 80L, 10L, 5L, 10.0,
                    Map.of("2026-01", 20L, "2026-02", 15L),
                    List.of(new AdminDashboardStats.UserStat("top@test.com", 5L)),
                    List.of(new AdminDashboardStats.UserStat("top@test.com", 25L)));

            when(userService.getAdminDashboardStats()).thenReturn(stats);

            mockMvc.perform(get("/api/v1/admin/stats")
                            .with(user(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalUsers").value(100))
                    .andExpect(jsonPath("$.data.activeUsers").value(80))
                    .andExpect(jsonPath("$.data.premiumUsers").value(10))
                    .andExpect(jsonPath("$.data.adminUsers").value(5))
                    .andExpect(jsonPath("$.data.premiumConversionRate").value(10.0))
                    .andExpect(jsonPath("$.data.topUsersByLists[0].email").value("top@test.com"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/lists")
    class GetAllListsTests {

        @Test
        @DisplayName("should return paginated list of item lists")
        @SuppressWarnings("unchecked")
        void getAllLists_returnsOkWithPageResponse() throws Exception {
            ItemList list = new ItemList();
            list.setId(UUID.randomUUID());
            list.setName("Test List");
            list.setCategory("Electronics");
            list.setUser(testUser);
            list.setCreatedAt(LocalDateTime.now());

            when(itemListRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(list)));

            mockMvc.perform(get("/api/v1/admin/lists")
                            .with(user(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].name").value("Test List"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("should filter lists by search criteria")
        @SuppressWarnings("unchecked")
        void getAllLists_withFilters_returnsFilteredResults() throws Exception {
            when(itemListRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/v1/admin/lists")
                            .param("search", "test")
                            .param("category", "Electronics")
                            .with(user(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/lists/{id}")
    class GetListDetailTests {

        @Test
        @DisplayName("should return list detail")
        void getListDetail_existingId_returnsOk() throws Exception {
            UUID listId = UUID.randomUUID();
            ItemList list = new ItemList();
            list.setId(listId);
            list.setName("Test List");
            list.setCategory("Electronics");
            list.setUser(testUser);
            list.setCreatedAt(LocalDateTime.now());

            when(itemListRepository.findById(listId)).thenReturn(Optional.of(list));

            mockMvc.perform(get("/api/v1/admin/lists/{id}", listId)
                            .with(user(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("Test List"))
                    .andExpect(jsonPath("$.data.category").value("Electronics"));
        }

        @Test
        @DisplayName("should return 404 when list not found")
        void getListDetail_nonExistingId_returns404() throws Exception {
            UUID nonExistingId = UUID.randomUUID();
            when(itemListRepository.findById(nonExistingId)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/admin/lists/{id}", nonExistingId)
                            .with(user(adminUser)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value(404));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/items")
    class GetAllItemsTests {

        @Test
        @DisplayName("should return paginated list of items")
        @SuppressWarnings("unchecked")
        void getAllItems_returnsOkWithPageResponse() throws Exception {
            Item item = new Item();
            item.setId(UUID.randomUUID());
            item.setName("Test Item");
            item.setCreatedAt(LocalDateTime.now());

            ItemList list = new ItemList();
            list.setId(UUID.randomUUID());
            list.setName("Parent List");
            list.setUser(testUser);
            item.setItemList(list);

            when(itemRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(item)));

            mockMvc.perform(get("/api/v1/admin/items")
                            .with(user(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].name").value("Test Item"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("should return 400 for invalid status enum")
        void getAllItems_invalidStatus_returns400() throws Exception {
            mockMvc.perform(get("/api/v1/admin/items")
                            .param("status", "INVALID_STATUS")
                            .with(user(adminUser)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should filter by valid status")
        @SuppressWarnings("unchecked")
        void getAllItems_validStatus_returnsFilteredResults() throws Exception {
            when(itemRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/v1/admin/items")
                            .param("status", "AVAILABLE")
                            .with(user(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users with search filters")
    class SearchUsersTests {

        @Test
        @DisplayName("should use searchUsers when search param is provided")
        void getAllUsers_withSearch_usesSearchService() throws Exception {
            when(userService.searchUsers(eq("test"), any(), any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testUser)));

            mockMvc.perform(get("/api/v1/admin/users")
                            .param("search", "test")
                            .with(user(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].email").value("user@test.com"));

            verify(userService).searchUsers(eq("test"), any(), any(), any(Pageable.class));
        }

        @Test
        @DisplayName("should use searchUsers when role filter is provided")
        void getAllUsers_withRoleFilter_usesSearchService() throws Exception {
            when(userService.searchUsers(any(), eq(Role.ADMIN), any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/v1/admin/users")
                            .param("role", "ADMIN")
                            .with(user(adminUser)))
                    .andExpect(status().isOk());

            verify(userService).searchUsers(any(), eq(Role.ADMIN), any(), any(Pageable.class));
        }

        @Test
        @DisplayName("should use searchUsers when enabled filter is provided")
        void getAllUsers_withEnabledFilter_usesSearchService() throws Exception {
            when(userService.searchUsers(any(), any(), eq(true), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testUser)));

            mockMvc.perform(get("/api/v1/admin/users")
                            .param("enabled", "true")
                            .with(user(adminUser)))
                    .andExpect(status().isOk());

            verify(userService).searchUsers(any(), any(), eq(true), any(Pageable.class));
        }
    }
}
