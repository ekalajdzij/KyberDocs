package com.kyberdocs.docs.users.dto;

import com.kyberdocs.docs.users.UserRole;
import com.kyberdocs.docs.users.UserStatus;

public class UserResponseDto {

    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private UserStatus status;
    private Integer inactivityTimeout;
    private String kyberPublicKeyHex;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Integer getInactivityTimeout() {
        return inactivityTimeout;
    }

    public void setInactivityTimeout(Integer inactivityTimeout) {
        this.inactivityTimeout = inactivityTimeout;
    }

    public String getKyberPublicKeyHex() {
        return kyberPublicKeyHex;
    }

    public void setKyberPublicKeyHex(String kyberPublicKeyHex) {
        this.kyberPublicKeyHex = kyberPublicKeyHex;
    }
}
