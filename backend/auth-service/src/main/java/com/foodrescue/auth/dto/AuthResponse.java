package com.foodrescue.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.foodrescue.auth.entity.Role;
// Lombok removed. Manual methods added below.

import java.util.UUID;

/**
 * Response DTO returned after successful authentication.
 * Contains JWT access token, refresh token, and basic user information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserInfo user;

    public AuthResponse() {}
    public AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresIn, UserInfo user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }

    // Manual builder pattern
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private long expiresIn;
        private UserInfo user;

        public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public Builder expiresIn(long expiresIn) { this.expiresIn = expiresIn; return this; }
        public Builder user(UserInfo user) { this.user = user; return this; }
        public AuthResponse build() {
            return new AuthResponse(accessToken, refreshToken, tokenType, expiresIn, user);
        }
    }

    public static class UserInfo {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
        private Role role;
        private String phone;
        private boolean verified;

        public UserInfo() {}
        public UserInfo(UUID id, String email, String firstName, String lastName, Role role, String phone, boolean verified) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
            this.phone = phone;
            this.verified = verified;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }

        // Manual builder pattern for UserInfo
        public static UserInfoBuilder builder() { return new UserInfoBuilder(); }
        public static class UserInfoBuilder {
            private UUID id;
            private String email;
            private String firstName;
            private String lastName;
            private Role role;
            private String phone;
            private boolean verified;

            public UserInfoBuilder id(UUID id) { this.id = id; return this; }
            public UserInfoBuilder email(String email) { this.email = email; return this; }
            public UserInfoBuilder firstName(String firstName) { this.firstName = firstName; return this; }
            public UserInfoBuilder lastName(String lastName) { this.lastName = lastName; return this; }
            public UserInfoBuilder role(Role role) { this.role = role; return this; }
            public UserInfoBuilder phone(String phone) { this.phone = phone; return this; }
            public UserInfoBuilder verified(boolean verified) { this.verified = verified; return this; }
            public UserInfo build() {
                return new UserInfo(id, email, firstName, lastName, role, phone, verified);
            }
        }
    }
}
