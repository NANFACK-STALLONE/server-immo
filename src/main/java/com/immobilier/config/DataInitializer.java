package com.immobilier.config;

import com.immobilier.entity.RoleEnum;
import com.immobilier.entity.User;
import com.immobilier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.email:admin@immo.com}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.fullname:Administrateur}")
    private String adminFullName;

    @Override
    public void run(String... args) {
        log.info("=== Initialisation des donnees de demarrage ===");
        initAdmin();
        log.info("=== Initialisation terminee ===");
    }

    private void initAdmin() {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("Compte admin non cree: APP_ADMIN_PASSWORD est vide.");
            return;
        }

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Compte admin deja existant ({}) - aucune action.", adminEmail);
            return;
        }

        if (userRepository.existsByUsername(adminUsername)) {
            log.info("Compte admin deja existant (username: {}) - aucune action.", adminUsername);
            return;
        }

        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .fullName(adminFullName)
                .role(RoleEnum.ROLE_ADMIN)
                .isActive(true)
                .build();

        userRepository.save(admin);

        log.info("========================================================");
        log.info("Compte ADMIN cree avec succes.");
        log.info("   Username : {}", adminUsername);
        log.info("   Email    : {}", adminEmail);
        log.info("   Password : defini via variable d'environnement");
        log.info("   Role     : ROLE_ADMIN");
        log.info("========================================================");
    }
}
