package com.example.splashstore.exceptions;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.getWriter().write(buildBody(request.getRequestURI()));
    }

    private String buildBody(String path) {
        return "{"
                + "\"timestamp\":\"" + Instant.now() + "\","
                + "\"status\":" + HttpStatus.FORBIDDEN.value() + ","
                + "\"error\":\"" + HttpStatus.FORBIDDEN.getReasonPhrase() + "\","
                + "\"message\":\"You do not have permission to access this resource\","
                + "\"path\":\"" + escape(path) + "\","
                + "\"validationErrors\":null"
                + "}";
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}


