package com.immobilier.service;

import com.immobilier.dto.UserDTO;
import com.immobilier.entity.RoleEnum;
import com.immobilier.entity.User;
import com.immobilier.exception.ResourceNotFoundException;
import com.immobilier.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

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
    }

    @Test
    public void testCreateUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User createdUser = userService.createUser("testuser", "test@example.com", "password123", "Test User", RoleEnum.ROLE_USER);

        assertNotNull(createdUser);
        assertEquals("testuser", createdUser.getUsername());
        assertEquals("test@example.com", createdUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testCreateUser_UsernameAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser("testuser", "test@example.com", "password123", "Test User", RoleEnum.ROLE_USER));
    }

    @Test
    public void testCreateUser_EmailAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                userService.createUser("testuser", "test@example.com", "password123", "Test User", RoleEnum.ROLE_USER));
    }

    @Test
    public void testGetUserById_Success() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));

        User user = userService.getUserById("user-id-1");

        assertNotNull(user);
        assertEquals("user-id-1", user.getId());
        assertEquals("testuser", user.getUsername());
        verify(userRepository, times(1)).findById("user-id-1");
    }

    @Test
    public void testGetUserById_UserNotFound() {
        when(userRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById("unknown-id"));
    }

    @Test
    public void testGetUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User user = userService.getUserByUsername("testuser");

        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    public void testGetUserByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User user = userService.getUserByEmail("test@example.com");

        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    public void testUpdateUser_Success() {
        UserDTO updateDTO = UserDTO.builder()
                .fullName("Updated User")
                .phone("1234567890")
                .address("123 Main St")
                .build();

        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User updatedUser = userService.updateUser("user-id-1", updateDTO);

        assertNotNull(updatedUser);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testToggleUserStatus_Success() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.toggleUserStatus("user-id-1", false);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testChangePassword_Success() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.changePassword("user-id-1", "oldPassword", "newPassword");

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testChangePassword_WrongOldPassword() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                userService.changePassword("user-id-1", "wrongPassword", "newPassword"));
    }

    @Test
    public void testConvertToDTO() {
        UserDTO userDTO = userService.convertToDTO(testUser);

        assertNotNull(userDTO);
        assertEquals(testUser.getId(), userDTO.getId());
        assertEquals(testUser.getUsername(), userDTO.getUsername());
        assertEquals(testUser.getEmail(), userDTO.getEmail());
    }

    @Test
    public void testDeleteUser_Success() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));

        userService.deleteUser("user-id-1");

        verify(userRepository, times(1)).delete(any(User.class));
    }
}
