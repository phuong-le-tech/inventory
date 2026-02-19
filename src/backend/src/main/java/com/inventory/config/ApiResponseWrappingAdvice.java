package com.inventory.config;

import com.inventory.dto.response.ApiDataResponse;
import com.inventory.dto.response.ApiErrorResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.inventory")
public class ApiResponseWrappingAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {
        // Don't wrap if already wrapped
        if (body instanceof ApiDataResponse<?> || body instanceof ApiErrorResponse) {
            return body;
        }

        // Don't wrap binary responses (e.g., images)
        if (body instanceof byte[]) {
            return body;
        }

        // Don't wrap null (204 No Content)
        if (body == null) {
            return null;
        }

        return ApiDataResponse.of(body);
    }
}
