package com.example.project.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist", indexes = {
        @Index(columnList = "tokenString", name = "idx_tokenblacklist_token")
})
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512, unique = true)
    private String tokenString;

    @Column(nullable = false)
    private LocalDateTime revokedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public TokenBlacklist() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTokenString() { return tokenString; }
    public void setTokenString(String tokenString) { this.tokenString = tokenString; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}