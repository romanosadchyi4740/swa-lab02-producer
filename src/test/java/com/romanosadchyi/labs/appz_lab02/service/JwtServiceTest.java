package com.romanosadchyi.labs.appz_lab02.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        byte[] keyBytes = java.util.Base64.getDecoder().decode(JwtService.SECRET);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    @DisplayName("Should validate token successfully when token is valid")
    void shouldValidateTokenSuccessfully() {
        // Given
        String token = createValidToken("testuser", "ROLE_USER");

        // When/Then
        assertDoesNotThrow(() -> jwtService.validateToken(token));
    }

    @Test
    @DisplayName("Should throw exception when token is invalid")
    void shouldThrowExceptionWhenTokenIsInvalid() {
        // Given
        String invalidToken = "invalid.token.here";

        // When/Then
        assertThrows(Exception.class, () -> jwtService.validateToken(invalidToken));
    }

    @Test
    @DisplayName("Should throw exception when token is expired")
    void shouldThrowExceptionWhenTokenIsExpired() {
        // Given
        String expiredToken = createExpiredToken("testuser", "ROLE_USER");

        // When/Then
        assertThrows(Exception.class, () -> jwtService.validateToken(expiredToken));
    }

    @Test
    @DisplayName("Should throw exception when token has wrong signature")
    void shouldThrowExceptionWhenTokenHasWrongSignature() {
        // Given
        String tokenWithWrongSignature = createTokenWithWrongSecret("testuser", "ROLE_USER");

        // When/Then
        assertThrows(Exception.class, () -> jwtService.validateToken(tokenWithWrongSignature));
    }

    @Test
    @DisplayName("Should extract username correctly from valid token")
    void shouldExtractUsernameCorrectly() {
        // Given
        String expectedUsername = "testuser";
        String token = createValidToken(expectedUsername, "ROLE_USER");

        // When
        String actualUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    @DisplayName("Should extract role correctly from valid token")
    void shouldExtractRoleCorrectly() {
        // Given
        String expectedRole = "ROLE_ADMIN";
        String token = createValidToken("testuser", expectedRole);

        // When
        String actualRole = jwtService.extractRole(token);

        // Then
        assertEquals(expectedRole, actualRole);
    }

    @Test
    @DisplayName("Should extract different roles correctly")
    void shouldExtractDifferentRolesCorrectly() {
        // Given
        String[] roles = {"ROLE_USER", "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_PARENT"};
        
        for (String role : roles) {
            String token = createValidToken("testuser", role);
            
            // When
            String extractedRole = jwtService.extractRole(token);
            
            // Then
            assertEquals(role, extractedRole, "Should extract role: " + role);
        }
    }

    @Test
    @DisplayName("Should throw exception when extracting username from invalid token")
    void shouldThrowExceptionWhenExtractingUsernameFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When/Then
        assertThrows(Exception.class, () -> jwtService.extractUsername(invalidToken));
    }

    @Test
    @DisplayName("Should throw exception when extracting role from invalid token")
    void shouldThrowExceptionWhenExtractingRoleFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When/Then
        assertThrows(Exception.class, () -> jwtService.extractRole(invalidToken));
    }

    @Test
    @DisplayName("Should handle empty token")
    void shouldHandleEmptyToken() {
        // Given
        String emptyToken = "";

        // When/Then
        assertThrows(Exception.class, () -> jwtService.validateToken(emptyToken));
        assertThrows(Exception.class, () -> jwtService.extractUsername(emptyToken));
        assertThrows(Exception.class, () -> jwtService.extractRole(emptyToken));
    }

    @Test
    @DisplayName("Should handle null token")
    void shouldHandleNullToken() {
        // Given
        String nullToken = null;

        // When/Then
        assertThrows(Exception.class, () -> jwtService.validateToken(nullToken));
        assertThrows(Exception.class, () -> jwtService.extractUsername(nullToken));
        assertThrows(Exception.class, () -> jwtService.extractRole(nullToken));
    }

    @Test
    @DisplayName("Should extract username and role from same token")
    void shouldExtractUsernameAndRoleFromSameToken() {
        // Given
        String username = "johndoe";
        String role = "ROLE_TEACHER";
        String token = createValidToken(username, role);

        // When
        String extractedUsername = jwtService.extractUsername(token);
        String extractedRole = jwtService.extractRole(token);

        // Then
        assertEquals(username, extractedUsername);
        assertEquals(role, extractedRole);
    }

    @Test
    @DisplayName("Should handle token without role claim")
    void shouldHandleTokenWithoutRoleClaim() {
        // Given
        String token = createTokenWithoutRole("testuser");

        // When
        String username = jwtService.extractUsername(token);
        
        // Then
        assertEquals("testuser", username);
        // Role should be null when not present
        assertNull(jwtService.extractRole(token));
    }

    private String createValidToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        
        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(secretKey)
                .compact();
    }

    private String createExpiredToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        
        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 2)) // 2 hours ago
                .expiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // 1 hour ago
                .signWith(secretKey)
                .compact();
    }

    private String createTokenWithWrongSecret(String username, String role) {
        byte[] wrongKeyBytes = Base64.getDecoder().decode("576D5A7134743757217A25432A462D4A614E645266556A586E3272357538782F");
        SecretKey wrongKey = Keys.hmacShaKeyFor(wrongKeyBytes);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        
        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(wrongKey)
                .compact();
    }

    private String createTokenWithoutRole(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(secretKey)
                .compact();
    }
}

