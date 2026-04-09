package com.immobilier.service;

import com.immobilier.dto.LoginRequest;
import com.immobilier.dto.LoginResponse;
import com.immobilier.entity.RoleEnum;
import com.immobilier.entity.User;
import com.immobilier.exception.ResourceNotFoundException;
import com.immobilier.repository.UserRepository;
import com.immobilier.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .id("user-id-1")
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .role(RoleEnum.ROLE_USER)
                .isActive(true)
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    public void testLogin_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(jwtTokenProvider.generateAccessToken(testUser)).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(testUser)).thenReturn("refreshToken");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400000L);
        doNothing().when(userService).updateLastLogin("user-id-1");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getUser());
        verify(jwtTokenProvider, times(1)).generateAccessToken(testUser);
        verify(jwtTokenProvider, times(1)).generateRefreshToken(testUser);
    }

    @Test
    public void testLogin_UserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();

        assertThrows(ResourceNotFoundException.class, () -> authService.login(request));
    }

    @Test
    public void testLogin_WrongPassword() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        LoginRequest wrongPasswordRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongPassword")
                .build();

        assertThrows(BadCredentialsException.class, () -> authService.login(wrongPasswordRequest));
    }

    @Test
    public void testLogin_InactiveUser() {
        testUser.setIsActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));
    }

    @Test
    public void testRegister_Success() {
        when(userService.createUser(anyString(), anyString(), anyString(), anyString(), any(RoleEnum.class)))
                .thenReturn(testUser);

        User registeredUser = authService.register("testuser", "test@example.com", "password123", "Test User");

        assertNotNull(registeredUser);
        assertEquals("testuser", registeredUser.getUsername());
        verify(userService, times(1)).createUser(anyString(), anyString(), anyString(), anyString(), any(RoleEnum.class));
    }

    @Test
    public void testRefreshToken_Success() {
        when(jwtTokenProvider.validateToken("refreshToken")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("refreshToken")).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(jwtTokenProvider.generateAccessToken(testUser)).thenReturn("newAccessToken");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400000L);

        LoginResponse response = authService.refreshToken("refreshToken");

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        verify(jwtTokenProvider, times(1)).generateAccessToken(testUser);
    }

    @Test
    public void testRefreshToken_InvalidToken() {
        when(jwtTokenProvider.validateToken("invalidToken")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.refreshToken("invalidToken"));
    }

    @Test
    public void testValidateAccessToken_Valid() {
        when(jwtTokenProvider.validateToken("accessToken")).thenReturn(true);
        when(jwtTokenProvider.isTokenExpired("accessToken")).thenReturn(false);

        assertTrue(authService.validateAccessToken("accessToken"));
    }

    @Test
    public void testValidateAccessToken_Expired() {
        when(jwtTokenProvider.validateToken("expiredToken")).thenReturn(true);
        when(jwtTokenProvider.isTokenExpired("expiredToken")).thenReturn(true);

        assertFalse(authService.validateAccessToken("expiredToken"));
    }

    @Test
    public void testGetUserFromToken_Success() {
        when(jwtTokenProvider.getUsernameFromToken("accessToken")).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);

        User user = authService.getUserFromToken("accessToken");

        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
    }
}
