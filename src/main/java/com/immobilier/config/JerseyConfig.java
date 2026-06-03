package com.immobilier.config;

import com.immobilier.exception.mapper.AccessDeniedExceptionMapper;
import com.immobilier.exception.mapper.BadCredentialsExceptionMapper;
import com.immobilier.exception.mapper.GlobalExceptionMapper;
import com.immobilier.exception.mapper.IllegalArgumentExceptionMapper;
import com.immobilier.exception.mapper.ResourceNotFoundExceptionMapper;
import com.immobilier.exception.mapper.ValidationExceptionMapper;
import com.immobilier.resource.AuthResource;
import com.immobilier.resource.InteractionResource;
import com.immobilier.resource.NotificationResource;
import com.immobilier.resource.PropertyResource;
import com.immobilier.resource.RoleRequestResource;
import com.immobilier.resource.UserResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration principale de JAX-RS / Jersey.
 *
 * Les resources et providers sont enregistres explicitement pour rester
 * compatibles avec le JAR executable Spring Boot utilise par Docker.
 */
@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(AuthResource.class);
        register(InteractionResource.class);
        register(NotificationResource.class);
        register(PropertyResource.class);
        register(RoleRequestResource.class);
        register(UserResource.class);

        register(AccessDeniedExceptionMapper.class);
        register(BadCredentialsExceptionMapper.class);
        register(GlobalExceptionMapper.class);
        register(IllegalArgumentExceptionMapper.class);
        register(ResourceNotFoundExceptionMapper.class);
        register(ValidationExceptionMapper.class);

        register(JacksonFeature.class);

        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        property(ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, true);
    }
}
