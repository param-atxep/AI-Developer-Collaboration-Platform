package com.foodrescue.auth.service;

import com.foodrescue.auth.dto.AuthResponse;
import com.foodrescue.auth.dto.LoginRequest;
import com.foodrescue.auth.dto.RegisterRequest;
import com.foodrescue.auth.dto.TokenRefreshRequest;

/**
 * Service interface defining authentication and authorization operations.
 */
public interface AuthService {

    /**
     * Registers a new user account.
     *
     * @param request the registration details
     * @return an {@link AuthResponse} containing tokens and user information
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user with email and password credentials.
     *
     * @param request the login credentials
     * @return an {@link AuthResponse} containing tokens and user information
     */
    AuthResponse login(LoginRequest request);

    /**
     * Issues a new access token using a valid refresh token.
     *
     * @param request the token refresh request containing the refresh token
     * @return an {@link AuthResponse} containing the new access token
     */
    AuthResponse refreshToken(TokenRefreshRequest request);

    /**
     * Validates a JWT access token.
     *
     * @param token the JWT token string to validate
     * @return true if the token is valid and not expired
     */
    boolean validateToken(String token);
}
