package com.foodrescue.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Provides JWT token generation and validation using RS256 (RSA with SHA-256).
 * <p>
 * An RSA key pair is generated on startup and held in memory.
 * In a production multi-instance deployment the key pair should be loaded from
 * a shared secret store (e.g. Vault, AWS KMS) so that every instance can
 * validate tokens issued by any other instance.
 * </p>
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.access-token-expiration-ms:900000}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    @Value("${jwt.issuer:foodrescue-auth-service}")
    private String issuer;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    /**
     * Generates an RSA-2048 key pair on application startup.
     */
    @PostConstruct
    public void init() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            log.info("RSA key pair generated successfully for JWT signing");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to generate RSA key pair for JWT signing", e);
        }
    }

    /**
     * Generates a JWT access token for the given user.
     *
     * @param userId    the user's unique identifier
     * @param email     the user's email address
     * @param role      the user's role
     * @return a signed JWT access token string
     */
    public String generateAccessToken(UUID userId, String email, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenExpirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claims(Map.of(
                        "email", email,
                        "role", role,
                        "type", "access"
                ))
                .signWith(privateKey)
                .compact();
    }

    /**
     * Generates a cryptographically random refresh token string.
     *
     * @return a unique refresh token value
     */
    public String generateRefreshTokenValue() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }

    /**
     * Validates a JWT token and returns its claims if valid.
     *
     * @param token the JWT token string to validate
     * @return the parsed claims
     * @throws JwtException if the token is invalid, expired, or malformed
     */
    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks whether a JWT token is valid without throwing exceptions.
     *
     * @param token the JWT token string to check
     * @return true if the token is valid and not expired
     */
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty or null: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extracts the subject (user ID) from a JWT token.
     *
     * @param token the JWT token string
     * @return the subject claim (user ID as string)
     */
    public String getSubject(String token) {
        return validateToken(token).getSubject();
    }

    /**
     * Extracts the email claim from a JWT token.
     *
     * @param token the JWT token string
     * @return the email address
     */
    public String getEmail(String token) {
        return validateToken(token).get("email", String.class);
    }

    /**
     * Extracts the role claim from a JWT token.
     *
     * @param token the JWT token string
     * @return the role string
     */
    public String getRole(String token) {
        return validateToken(token).get("role", String.class);
    }

    /**
     * Returns the configured access token expiration in milliseconds.
     *
     * @return expiration time in milliseconds
     */
    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    /**
     * Returns the configured refresh token expiration in milliseconds.
     *
     * @return expiration time in milliseconds
     */
    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    /**
     * Returns the public key for external token verification.
     *
     * @return the RSA public key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
}
