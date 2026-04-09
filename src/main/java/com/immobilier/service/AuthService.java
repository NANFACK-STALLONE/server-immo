package com.immobilier.service;

import com.immobilier.dto.LoginRequest;
import com.immobilier.dto.LoginResponse;
import com.immobilier.entity.User;
import com.immobilier.entity.RoleEnum;
import com.immobilier.exception.ResourceNotFoundException;
import com.immobilier.repository.UserRepository;
import com.immobilier.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Tentative de connexion pour: {}", loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Mot de passe incorrect pour: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Le compte utilisateur est désactivé");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword())
            );

            userService.updateLastLogin(user.getId());

            String accessToken = jwtTokenProvider.generateAccessToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            log.info("Connexion réussie pour: {}", loginRequest.getEmail());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationTime())
                    .user(LoginResponse.UserDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .fullName(user.getFullName())
                            .role(user.getRole())
                            .build())
                    .build();

        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification: {}", e.getMessage());
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }
    }

    public User register(String username, String email, String password, String fullName) {
        log.info("Enregistrement d'un nouvel utilisateur: {}", email);
        return userService.createUser(username, email, password, fullName, RoleEnum.ROLE_USER);
    }

    public LoginResponse refreshToken(String refreshToken) {
        log.info("Rafraîchissement du token");

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh token invalide ou expiré");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userService.getUserByUsername(username);
        String accessToken = jwtTokenProvider.generateAccessToken(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .build();
    }

    public boolean validateAccessToken(String token) {
        return jwtTokenProvider.validateToken(token) && !jwtTokenProvider.isTokenExpired(token);
    }

    public User getUserFromToken(String token) {
        String username = jwtTokenProvider.getUsernameFromToken(token);
        return userService.getUserByUsername(username);
    }
}
