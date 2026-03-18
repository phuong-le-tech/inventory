package com.inventory.exception;

import java.util.UUID;

public class WorkspaceNotFoundException extends RuntimeException {
    public WorkspaceNotFoundException(UUID id) {
        super("Workspace not found with id: " + id);
    }
}
