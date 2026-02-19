package com.inventory.dto.response;

public record ApiErrorResponse(ApiError error) {

    public record ApiError(int code, String message) {
    }

    public static ApiErrorResponse of(int code, String message) {
        return new ApiErrorResponse(new ApiError(code, message));
    }
}
