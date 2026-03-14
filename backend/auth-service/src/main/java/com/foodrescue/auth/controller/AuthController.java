package com.foodrescue.auth.controller;

import com.foodrescue.auth.dto.AuthResponse;
import com.foodrescue.auth.dto.LoginRequest;
import com.foodrescue.auth.dto.RegisterRequest;
import com.foodrescue.auth.dto.TokenRefreshRequest;
import com.foodrescue.auth.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller exposing authentication and authorization endpoints.
 * <p>
 * All endpoints under {@code /api/auth/**} are publicly accessible as
 * configured in {@link com.foodrescue.auth.security.SecurityConfig}.
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account.
     * <p>
     * POST /api/auth/register
     *
     * @param request the registration payload with validated fields
     * @return 201 Created with tokens and user information
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user with email and password.
     * <p>
     * POST /api/auth/login
     *
     * @param request the login credentials
     * @return 200 OK with tokens and user information
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes an access token using a valid refresh token.
     * <p>
     * POST /api/auth/refresh
     *
     * @param request the refresh token payload
     * @return 200 OK with new tokens and user information
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.debug("Token refresh request received");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Validates a JWT access token.
     * <p>
     * POST /api/auth/validate?token=...
     *
     * @param token the JWT token string to validate
     * @return 200 OK with a JSON body indicating whether the token is valid
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam("token") String token) {
        log.debug("Token validation request received");
        boolean valid = authService.validateToken(token);

        Map<String, Object> response = Map.of(
                "valid", valid,
                "token", maskToken(token)
        );

        if (valid) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Masks a token for safe inclusion in log messages and responses.
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 6);
    }
}
