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

/**
 * DataInitializer — s'exécute automatiquement au démarrage de l'application.
 *
 * Vérifie si un compte ADMIN existe déjà dans MongoDB.
 * → S'il n'existe pas : le crée automatiquement avec les credentials
 *   définis dans application.properties.
 * → S'il existe déjà : ne fait rien (idempotent).
 *
 * Implémente CommandLineRunner → Spring Boot l'exécute après le
 * démarrage complet du contexte applicatif (MongoDB connecté, beans prêts).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    // Credentials lus depuis application.properties
    // (avec valeurs par défaut si la propriété est absente)
    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.email:admin@immo.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@2024!}")
    private String adminPassword;

    @Value("${app.admin.fullname:Administrateur}")
    private String adminFullName;

    @Override
    public void run(String... args) {
        log.info("=== Initialisation des données de démarrage ===");
        initAdmin();
        log.info("=== Initialisation terminée ===");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Création du compte Admin
    // ─────────────────────────────────────────────────────────────────────────
    private void initAdmin() {

        // Vérification 1 : admin déjà présent par email
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("✅ Compte admin déjà existant ({}) — aucune action.", adminEmail);
            return;
        }

        // Vérification 2 : admin déjà présent par username
        if (userRepository.existsByUsername(adminUsername)) {
            log.info("✅ Compte admin déjà existant (username: {}) — aucune action.", adminUsername);
            return;
        }

        // Aucun admin → on crée le compte
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
        log.info("🔐 Compte ADMIN créé avec succès !");
        log.info("   Username : {}", adminUsername);
        log.info("   Email    : {}", adminEmail);
        log.info("   Password : {}", adminPassword);
        log.info("   Rôle     : ROLE_ADMIN");
        log.info("⚠️  Pensez à changer le mot de passe en production !");
        log.info("========================================================");
    }
}
