package com.immobilier.config;

import com.immobilier.security.JwtAuthenticationFilter;
import com.immobilier.security.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration Spring Security pour une API JAX-RS (Jersey) stateless.
 *
 * Spring Security s'exécute en AMONT de Jersey (c'est un filtre Servlet).
 * Il vérifie le token JWT et peuple le SecurityContextHolder AVANT que
 * la requête n'atteigne les resources JAX-RS.
 *
 * Les annotations @PreAuthorize sur les resources JAX-RS fonctionnent grâce
 * à @EnableGlobalMethodSecurity(prePostEnabled = true) qui active l'AOP Spring.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true,   // active @PreAuthorize / @PostAuthorize
        securedEnabled = true,   // active @Secured
        jsr250Enabled  = true    // active @RolesAllowed (JSR-250)
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuration CORS explicite — obligatoire avec Jersey (spring-boot-starter-jersey
     * ne fournit pas d'auto-configuration CORS contrairement à spring-boot-starter-web).
     *
     * Autorise toutes les origines pour le développement.
     * En production : remplacer "*" par les domaines autorisés.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Origin",
                "X-Requested-With", "Cache-Control"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // ── CORS (Jersey gère les requêtes cross-origin via le filtre) ──
            .cors()
            .and()

            // ── Désactiver CSRF (API REST stateless, pas de session) ─────────
            .csrf().disable()

            // ── Gestionnaire d'erreurs 401 ───────────────────────────────────
            .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
            .and()

            // ── Pas de session HTTP (JWT = stateless) ────────────────────────
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()

            // ── Règles d'autorisation par URL ────────────────────────────────
            .authorizeRequests()

                // Ressources statiques
                .antMatchers(
                    "/", "/favicon.ico",
                    "/**/*.png", "/**/*.gif", "/**/*.svg",
                    "/**/*.jpg", "/**/*.html", "/**/*.css", "/**/*.js"
                ).permitAll()

                // Auth : totalement public (login, register, refresh, validate, health)
                .antMatchers("/api/auth/**").permitAll()

                // Properties : lecture publique sans token
                .antMatchers(HttpMethod.GET, "/api/properties/public").permitAll()
                .antMatchers(HttpMethod.GET, "/api/properties/search").permitAll()
                .antMatchers(HttpMethod.GET, "/api/properties/{id}").permitAll()

                // Tout le reste requiert une authentification
                .anyRequest().authenticated();

        // ── Filtre JWT avant UsernamePasswordAuthenticationFilter ────────────
        // Ce filtre extrait le token JWT du header Authorization,
        // valide la signature et peuple le SecurityContextHolder.
        // Jersey reçoit ensuite la requête avec le principal déjà positionné.
        http.addFilterBefore(
            jwtAuthenticationFilter(),
            UsernamePasswordAuthenticationFilter.class
        );
    }
}
