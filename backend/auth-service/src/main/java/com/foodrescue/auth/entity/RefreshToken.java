package com.foodrescue.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
// Lombok removed. Manual methods added below.

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a refresh token.
 * Used to issue new access tokens without requiring the user to re-authenticate.
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_token", columnList = "token", unique = true),
        @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id")
})
public class RefreshToken {

    private UUID id;
    private String token;
    private User user;
    private Instant expiryDate;
    private boolean revoked = false;
    private Instant createdAt;

    public RefreshToken() {}

    public RefreshToken(UUID id, String token, User user, Instant expiryDate, boolean revoked, Instant createdAt) {
        this.id = id;
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.revoked = revoked;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // Manual builder pattern
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private UUID id;
        private String token;
        private User user;
        private Instant expiryDate;
        private boolean revoked = false;
        private Instant createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder token(String token) { this.token = token; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder expiryDate(Instant expiryDate) { this.expiryDate = expiryDate; return this; }
        public Builder revoked(boolean revoked) { this.revoked = revoked; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public RefreshToken build() {
            return new RefreshToken(id, token, user, expiryDate, revoked, createdAt);
        }
    }
}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Checks whether this refresh token has expired.
     *
     * @return true if the token expiry date is before the current instant
     */
    public boolean isExpired() {
        return this.expiryDate.isBefore(Instant.now());
    }

    /**
     * Checks whether this refresh token is usable (not expired and not revoked).
     *
     * @return true if the token is still valid
     */
    public boolean isValid() {
        return !isExpired() && !revoked;
    }

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
