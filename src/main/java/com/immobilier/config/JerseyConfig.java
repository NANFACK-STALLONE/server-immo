package com.immobilier.config;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration principale de JAX-RS / Jersey.
 *
 * On utilise packages() au lieu de register(XxxResource.class) pour que
 * Jersey découvre automatiquement les classes @Path et @Provider via le
 * scan de packages, tout en utilisant l'injection Spring (DI correcte).
 *
 * @ApplicationPath est supprimé : le chemin de base est géré par
 * spring.jersey.application-path=/ dans application.properties,
 * ce qui évite le conflit entre l'annotation et la propriété.
 */
@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {

        // Scan automatique des @Path (resources) et @Provider (mappers)
        packages(
            "com.immobilier.resource",        // AuthResource, UserResource, PropertyResource
            "com.immobilier.exception.mapper"  // ExceptionMappers JAX-RS
        );

        // Sérialisation JSON via Jackson
        register(JacksonFeature.class);

        // Bean Validation : renvoyer le détail des erreurs dans la réponse
        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        property(ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, true);
    }
}
