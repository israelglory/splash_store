package com.example.splashstore.dto;

public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String role;
    private String fullname;
    private String phone;

    public UserResponse(Long id,
                        String email, String username, String role, String fullname, String phone){
        this.email = email;
        this.username = username;
        this.role = role;
        this.fullname = fullname;
        this.id = id;
        this.phone =  phone;


    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getFullname() {
        return fullname;
    }

    public String getPhone() {
        return phone;
    }
}
