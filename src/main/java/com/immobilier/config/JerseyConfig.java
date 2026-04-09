package com.immobilier.config;

import com.immobilier.exception.mapper.AccessDeniedExceptionMapper;
import com.immobilier.exception.mapper.BadCredentialsExceptionMapper;
import com.immobilier.exception.mapper.GlobalExceptionMapper;
import com.immobilier.exception.mapper.IllegalArgumentExceptionMapper;
import com.immobilier.exception.mapper.ResourceNotFoundExceptionMapper;
import com.immobilier.exception.mapper.ValidationExceptionMapper;
import com.immobilier.resource.AuthResource;
import com.immobilier.resource.PropertyResource;
import com.immobilier.resource.UserResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

/**
 * Configuration principale de JAX-RS / Jersey.
 *
 * Remplace toute la configuration Spring MVC (@RestController, @RequestMapping…).
 * Ici on enregistre manuellement :
 *   - les Resources (endpoints)
 *   - les ExceptionMappers (gestion centralisée des erreurs)
 *   - JacksonFeature (sérialisation JSON)
 */
@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {

        // ── Resources (endpoints JAX-RS) ──────────────────────────────
        register(AuthResource.class);
        register(UserResource.class);
        register(PropertyResource.class);

        // ── ExceptionMappers (remplacent @RestControllerAdvice) ───────
        register(ResourceNotFoundExceptionMapper.class);
        register(BadCredentialsExceptionMapper.class);
        register(AccessDeniedExceptionMapper.class);
        register(IllegalArgumentExceptionMapper.class);
        register(ValidationExceptionMapper.class);
        register(GlobalExceptionMapper.class);

        // ── JSON via Jackson ──────────────────────────────────────────
        register(JacksonFeature.class);

        // Activer la validation Bean (JSR-380) côté JAX-RS
        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        property(ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, true);
    }
}
