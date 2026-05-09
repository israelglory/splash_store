package com.example.splashstore.exceptions;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(buildBody(request.getRequestURI()));
    }

    private String buildBody(String path) {
        return "{"
                + "\"timestamp\":\"" + Instant.now() + "\","
                + "\"status\":" + HttpStatus.UNAUTHORIZED.value() + ","
                + "\"error\":\"" + HttpStatus.UNAUTHORIZED.getReasonPhrase() + "\","
                + "\"message\":\"Authentication is required to access this resource\","
                + "\"path\":\"" + escape(path) + "\","
                + "\"validationErrors\":null"
                + "}";
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}


