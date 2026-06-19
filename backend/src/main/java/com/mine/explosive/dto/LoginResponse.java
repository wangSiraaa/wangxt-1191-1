package com.mine.explosive.dto;

import com.mine.explosive.enums.Role;




public class LoginResponse {
    public LoginResponse() {
    }
    public LoginResponse(String token, String username, String name, Role role, Long userId) {
        this.token = token;
        this.username = username;
        this.name = name;
        this.role = role;
        this.userId = userId;
    }
    private String token;
    private String username;
    private String name;
    private Role role;
    private Long userId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
