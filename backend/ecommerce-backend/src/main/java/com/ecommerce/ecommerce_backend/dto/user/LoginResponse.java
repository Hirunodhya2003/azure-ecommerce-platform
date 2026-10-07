package com.ecommerce.ecommerce_backend.dto.user;

public class LoginResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private Boolean active;
    private String token;

    public LoginResponse() {
    }

    public LoginResponse(
            Long id,
            String name,
            String email,
            String role,
            Boolean active,
            String token
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}