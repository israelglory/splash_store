package com.example.splashstore.config;

import com.example.splashstore.dto.ApiSuccessResponse;
import com.example.splashstore.exceptions.ApiErrorResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.net.URI;

@RestControllerAdvice
public class ApiSuccessResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response
    ) {
        if (isSwaggerRequest(request)) {
            return body;
        }

        if (!isSuccessStatus(response)) {
            return body;
        }

        if (body instanceof ApiSuccessResponse<?> || body instanceof ApiErrorResponse) {
            return body;
        }

        ApiSuccessResponse<Object> wrapped = new ApiSuccessResponse<>(body);

        if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            // Avoid converter conflicts for plain String endpoints.
            return body;
        }

        return wrapped;
    }

    private boolean isSwaggerRequest(ServerHttpRequest request) {
        URI uri = request.getURI();
        String path = uri.getPath();
        return path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html");
    }

    private boolean isSuccessStatus(ServerHttpResponse response) {
        if (response instanceof ServletServerHttpResponse servletResponse) {
            int status = servletResponse.getServletResponse().getStatus();
            return status >= 200 && status < 300;
        }
        return true;
    }
}





