package com.foodrescue.auth.service;

import com.foodrescue.auth.dto.AuthResponse;
import com.foodrescue.auth.dto.LoginRequest;
import com.foodrescue.auth.dto.RegisterRequest;
import com.foodrescue.auth.dto.TokenRefreshRequest;
import com.foodrescue.auth.entity.RefreshToken;
import com.foodrescue.auth.entity.User;
import com.foodrescue.auth.exception.TokenRefreshException;
import com.foodrescue.auth.repository.RefreshTokenRepository;
import com.foodrescue.auth.repository.UserRepository;
import com.foodrescue.auth.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Implementation of {@link AuthService} that handles user registration,
 * authentication, token refresh, and token validation.
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if a user with the given email already exists
     */
    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email already in use - {}", request.getEmail());
            throw new IllegalArgumentException("An account with this email address already exists");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .role(request.getRole())
                .phone(request.getPhone())
                .verified(false)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully with id: {}", user.getId());

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());

        RefreshToken refreshToken = createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    /**
     * {@inheritDoc}
     *
     * @throws BadCredentialsException if the email is not found or the password does not match
     */
    @Override
    @Transactional(readOnly = false)
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for email - {}", request.getEmail());
                    return new BadCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid password for email - {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        // Revoke any existing refresh tokens for this user
        refreshTokenRepository.revokeAllByUser(user);

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());

        RefreshToken refreshToken = createRefreshToken(user);

        log.info("User logged in successfully: {}", user.getId());
        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    /**
     * {@inheritDoc}
     *
     * @throws TokenRefreshException if the refresh token is not found, expired, or revoked
     */
    @Override
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        log.debug("Processing token refresh request");

        String requestToken = request.getRefreshToken();

        RefreshToken storedToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> {
                    log.warn("Token refresh failed: token not found");
                    return new TokenRefreshException(requestToken, "Refresh token not found");
                });

        if (storedToken.isRevoked()) {
            log.warn("Token refresh failed: token has been revoked for user {}", storedToken.getUser().getId());
            throw new TokenRefreshException(requestToken, "Refresh token has been revoked");
        }

        if (storedToken.isExpired()) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            log.warn("Token refresh failed: token has expired for user {}", storedToken.getUser().getId());
            throw new TokenRefreshException(requestToken, "Refresh token has expired. Please login again.");
        }

        User user = storedToken.getUser();

        // Revoke the old refresh token (rotation)
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        // Issue new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());

        RefreshToken newRefreshToken = createRefreshToken(user);

        log.info("Token refreshed successfully for user: {}", user.getId());
        return buildAuthResponse(user, newAccessToken, newRefreshToken.getToken());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return jwtTokenProvider.isTokenValid(token);
    }

    /**
     * Creates and persists a new refresh token for the given user.
     *
     * @param user the user to create the refresh token for
     * @return the persisted {@link RefreshToken} entity
     */
    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(jwtTokenProvider.generateRefreshTokenValue())
                .expiryDate(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs()))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Builds the authentication response DTO from user and token data.
     *
     * @param user              the authenticated user
     * @param accessToken       the JWT access token
     * @param refreshTokenValue the refresh token string
     * @return a fully populated {@link AuthResponse}
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshTokenValue) {
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .phone(user.getPhone())
                .verified(user.isVerified())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .user(userInfo)
                .build();
    }
}
