package com.kyberdocs.docs.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "last_heartbeat", nullable = false)
    private Timestamp lastHeartbeat;

    @Column(nullable = false)
    private Integer inactivity_timeout;

    @Enumerated(EnumType.STRING)
    @Column
    private UserStatus status;

    @JsonIgnore
    @Column(name = "kyber_public_key_hex", columnDefinition = "TEXT")
    private String kyberPublicKeyHex;

    @JsonIgnore
    @Column(name = "kyber_secret_key_hex", columnDefinition = "TEXT")
    private String kyberSecretKeyHex;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Timestamp getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Timestamp lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Integer getInactivityTimeout() {
        return inactivity_timeout;
    }

    public void setInactivityTimeout(Integer inactivity_timeout) {
        this.inactivity_timeout = inactivity_timeout;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getKyberPublicKeyHex() {
        return kyberPublicKeyHex;
    }

    public void setKyberPublicKeyHex(String kyberPublicKeyHex) {
        this.kyberPublicKeyHex = kyberPublicKeyHex;
    }

    public String getKyberSecretKeyHex() {
        return kyberSecretKeyHex;
    }

    public void setKyberSecretKeyHex(String kyberSecretKeyHex) {
        this.kyberSecretKeyHex = kyberSecretKeyHex;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }



}
