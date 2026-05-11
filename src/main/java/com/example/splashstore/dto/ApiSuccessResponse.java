package com.example.splashstore.dto;

public record ApiSuccessResponse<T>(T data, String message) {
    public ApiSuccessResponse(T data) {
        this(data, "Success");
    }
}

