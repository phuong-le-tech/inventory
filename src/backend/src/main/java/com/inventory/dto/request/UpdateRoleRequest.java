package com.inventory.dto.request;

import com.inventory.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
    @NotNull(message = "Role is required")
    Role role
) {}
