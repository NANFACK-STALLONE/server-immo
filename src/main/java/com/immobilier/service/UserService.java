package com.immobilier.service;

import com.immobilier.dto.UserDTO;
import com.immobilier.entity.User;
import com.immobilier.entity.RoleEnum;
import com.immobilier.exception.ResourceNotFoundException;
import com.immobilier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(String username, String email, String password, String fullName, RoleEnum role) {
        log.info("Création d'un nouvel utilisateur: {}", username);

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Le nom d'utilisateur existe déjà: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("L'email existe déjà: " + email);
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(role != null ? role : RoleEnum.ROLE_USER)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + username));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email: " + email));
    }

    public User updateUser(String id, UserDTO userDTO) {
        User user = getUserById(id);
        log.info("Mise à jour de l'utilisateur: {}", id);

        if (userDTO.getFullName() != null) user.setFullName(userDTO.getFullName());
        if (userDTO.getPhone() != null) user.setPhone(userDTO.getPhone());
        if (userDTO.getAddress() != null) user.setAddress(userDTO.getAddress());

        return userRepository.save(user);
    }

    public User toggleUserStatus(String id, Boolean isActive) {
        User user = getUserById(id);
        log.info("Changement du statut de l'utilisateur {} à {}", id, isActive);
        user.setIsActive(isActive);
        return userRepository.save(user);
    }

    public void updateLastLogin(String userId) {
        User user = getUserById(userId);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("L'ancien mot de passe est incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Mot de passe changé pour l'utilisateur: {}", userId);
    }

    public void resetPassword(String email, String newPassword) {
        User user = getUserByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Mot de passe réinitialisé pour: {}", email);
    }

    public void deleteUser(String id) {
        User user = getUserById(id);
        log.info("Suppression de l'utilisateur: {}", id);
        userRepository.delete(user);
    }

    public UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .isActive(user.getIsActive())
                .build();
    }

    public User convertToEntity(UserDTO userDTO) {
        return User.builder()
                .id(userDTO.getId())
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .fullName(userDTO.getFullName())
                .phone(userDTO.getPhone())
                .address(userDTO.getAddress())
                .role(userDTO.getRole())
                .isActive(true)
                .build();
    }
}
