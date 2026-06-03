package com.immobilier.security;

import com.immobilier.entity.User;
import com.immobilier.entity.RoleEnum;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String jwtSecret = "mySecretKeyForTestingPurposesLongerThan64CharactersForHS512Security!@#$123456";
    private long jwtExpiration = 86400000; // 24 hours

    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .id("user-id-1")
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .fullName("Test User")
                .role(RoleEnum.ROLE_USER)
                .isActive(true)
                .build();

        // Initialiser les propriétés via réflexion
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", jwtExpiration);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", 604800000L);
    }

    @Test
    public void testGenerateAccessToken_Success() {
        // Act
        String token = jwtTokenProvider.generateAccessToken(testUser);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    public void testGenerateRefreshToken_Success() {
        // Act
        String token = jwtTokenProvider.generateRefreshToken(testUser);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    public void testValidateToken_ValidToken() {
        // Arrange
        String token = jwtTokenProvider.generateAccessToken(testUser);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidateToken_InvalidToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("invalidToken");

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testGetUsernameFromToken_Success() {
        // Arrange
        String token = jwtTokenProvider.generateAccessToken(testUser);

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    public void testGetEmailFromToken_Success() {
        // Arrange
        String token = jwtTokenProvider.generateAccessToken(testUser);

        // Act
        String email = jwtTokenProvider.getEmailFromToken(token);

        // Assert
        assertEquals("test@example.com", email);
    }

    @Test
    public void testGetRoleFromToken_Success() {
        // Arrange
        String token = jwtTokenProvider.generateAccessToken(testUser);

        // Act
        String role = jwtTokenProvider.getRoleFromToken(token);

        // Assert
        assertEquals("ROLE_USER", role);
    }

    @Test
    public void testGetExpirationDateFromToken_Success() {
        // Arrange
        String token = jwtTokenProvider.generateAccessToken(testUser);

        // Act
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        // Assert
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    public void testIsTokenExpired_NotExpired() {
        // Arrange
        String token = jwtTokenProvider.generateAccessToken(testUser);

        // Act
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    public void testGetExpirationTime() {
        // Act
        long expirationTime = jwtTokenProvider.getExpirationTime();

        // Assert
        assertEquals(jwtExpiration, expirationTime);
    }

    @Test
    public void testGenerateTokenWithDifferentRoles() {
        // Arrange
        testUser.setRole(RoleEnum.ROLE_ADMIN);

        // Act
        String token = jwtTokenProvider.generateAccessToken(testUser);
        String role = jwtTokenProvider.getRoleFromToken(token);

        // Assert
        assertEquals("ROLE_ADMIN", role);
    }

    @Test
    public void testTokenContainsAllClaims() {
        // Arrange
        String token = jwtTokenProvider.generateAccessToken(testUser);

        // Act & Assert
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token));
        assertEquals("test@example.com", jwtTokenProvider.getEmailFromToken(token));
        assertEquals("ROLE_USER", jwtTokenProvider.getRoleFromToken(token));
        assertNotNull(jwtTokenProvider.getExpirationDateFromToken(token));
    }
}
