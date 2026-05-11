package com.example.splashstore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoryRequest {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
