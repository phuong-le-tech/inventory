package com.inventory.controller;

import com.inventory.dto.request.CreateUserRequest;
import com.inventory.dto.request.UpdateRoleRequest;
import com.inventory.dto.request.UpdateStatusRequest;
import com.inventory.dto.response.AdminDashboardStats;
import com.inventory.dto.response.AdminItemListResponse;
import com.inventory.dto.response.AdminItemResponse;
import com.inventory.dto.response.AdminUserDetailResponse;
import com.inventory.dto.response.ItemListResponse;
import com.inventory.dto.response.PageResponse;
import com.inventory.dto.response.UserResponse;
import com.inventory.enums.Role;
import com.inventory.model.Item;
import com.inventory.model.ItemList;
import com.inventory.model.User;
import com.inventory.repository.ItemListRepository;
import com.inventory.repository.ItemRepository;
import com.inventory.repository.specification.ItemListSpecification;
import com.inventory.repository.specification.ItemSpecification;
import com.inventory.dto.request.ItemSearchCriteria;
import com.inventory.exception.ItemListNotFoundException;
import com.inventory.security.CustomUserDetails;
import com.inventory.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "email", "role");
    private static final Set<String> ALLOWED_LIST_SORT_FIELDS = Set.of("createdAt", "name", "category");

    private final IUserService userService;
    private final ItemListRepository itemListRepository;
    private final ItemRepository itemRepository;

    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean enabled
    ) {
        size = Math.min(Math.max(size, 1), 100);
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "createdAt";
        }
        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        boolean hasFilters = search != null || role != null || enabled != null;
        Page<User> usersPage = hasFilters
            ? userService.searchUsers(search, role, enabled, pageable)
            : userService.getAllUsers(pageable);
        Page<UserResponse> responsePage = usersPage.map(UserResponse::fromEntity);

        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDetailResponse> getUserDetail(
            @PathVariable @NonNull UUID id
    ) {
        return ResponseEntity.ok(userService.getUserDetail(id));
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(
            @AuthenticationPrincipal CustomUserDetails admin,
            @Valid @RequestBody @NonNull CreateUserRequest request) {
        log.info("SECURITY: Admin {} creating user with email={}, role={}", admin.getEmail(), request.email(), request.role());
        User savedUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(UserResponse.fromEntity(savedUser));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable @NonNull UUID id) {
        log.info("SECURITY: Admin {} deleting user {}", admin.getEmail(), id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody @NonNull UpdateRoleRequest request
    ) {
        log.info("SECURITY: Admin {} updating role of user {} to {}", admin.getEmail(), id, request.role());
        User savedUser = userService.updateUserRole(id, request.role());
        return ResponseEntity.ok(UserResponse.fromEntity(savedUser));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody @NonNull UpdateStatusRequest request
    ) {
        log.info("SECURITY: Admin {} updating status of user {} to enabled={}", admin.getEmail(), id, request.enabled());
        User savedUser = userService.updateUserStatus(id, request.enabled());
        return ResponseEntity.ok(UserResponse.fromEntity(savedUser));
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<Void> triggerPasswordReset(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable @NonNull UUID id
    ) {
        log.info("SECURITY: Admin {} triggering password reset for user {}", admin.getEmail(), id);
        userService.triggerPasswordReset(id);
        return ResponseEntity.ok().build();
    }

    // ── Analytics ──

    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStats> getDashboardStats() {
        return ResponseEntity.ok(userService.getAdminDashboardStats());
    }

    // ── Content Oversight (read-only) ──

    @GetMapping("/lists")
    public ResponseEntity<PageResponse<AdminItemListResponse>> getAllLists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID ownerId
    ) {
        size = Math.min(Math.max(size, 1), 100);
        if (!ALLOWED_LIST_SORT_FIELDS.contains(sortBy)) {
            sortBy = "createdAt";
        }
        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        boolean hasFilters = search != null || category != null || ownerId != null;
        Page<ItemList> listsPage = hasFilters
            ? itemListRepository.findAll(ItemListSpecification.withCriteria(search, category, ownerId), pageable)
            : itemListRepository.findAll(pageable);

        List<UUID> listIds = listsPage.getContent().stream().map(ItemList::getId).toList();
        Map<UUID, Long> itemCounts = listIds.isEmpty()
            ? Collections.emptyMap()
            : itemListRepository.countItemsByListIds(listIds).stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> (Long) row[1]));

        Page<AdminItemListResponse> responsePage = listsPage.map(il ->
            AdminItemListResponse.fromEntity(il, itemCounts.getOrDefault(il.getId(), 0L).intValue()));
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @GetMapping("/lists/{id}")
    public ResponseEntity<ItemListResponse> getListDetail(
            @PathVariable @NonNull UUID id
    ) {
        ItemList list = itemListRepository.findById(id)
            .orElseThrow(() -> new ItemListNotFoundException(id));
        return ResponseEntity.ok(ItemListResponse.fromEntity(list));
    }

    @GetMapping("/items")
    public ResponseEntity<PageResponse<AdminItemResponse>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID itemListId,
            @RequestParam(required = false) String status
    ) {
        size = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        com.inventory.enums.ItemStatus itemStatus = null;
        if (status != null) {
            try {
                itemStatus = com.inventory.enums.ItemStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        ItemSearchCriteria criteria = new ItemSearchCriteria(search, itemListId, itemStatus);
        // null userId = no user scope = admin sees all
        Page<Item> itemsPage = itemRepository.findAll(ItemSpecification.withCriteria(criteria, null), pageable);
        Page<AdminItemResponse> responsePage = itemsPage.map(AdminItemResponse::fromEntity);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }
}
