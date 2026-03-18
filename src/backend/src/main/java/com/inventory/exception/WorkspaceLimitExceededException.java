package com.inventory.exception;

public class WorkspaceLimitExceededException extends RuntimeException {
    public WorkspaceLimitExceededException(String message) {
        super(message);
    }
}
