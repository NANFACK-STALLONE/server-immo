package com.immobilier.exception.mapper;

import com.immobilier.exception.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.time.LocalDateTime;

/**
 * ExceptionMapper JAX-RS pour AccessDeniedException (Spring Security).
 *
 * Retourne 403 Forbidden quand l'utilisateur n'a pas le rôle requis.
 *
 * Exemple : un ROLE_BUYER tente d'accéder à POST /api/properties
 *           → Spring Security lève AccessDeniedException
 *           → Ce mapper retourne { "status": 403, "error": "Accès refusé", ... }
 */
@Provider
@Slf4j
public class AccessDeniedExceptionMapper
        implements ExceptionMapper<AccessDeniedException> {

    @Override
    public Response toResponse(AccessDeniedException ex) {
        log.warn("Accès refusé: {}", ex.getMessage());

        ApiError error = new ApiError(
                403,
                "Accès refusé",
                "Vous n'avez pas les permissions nécessaires pour cette action",
                "",
                LocalDateTime.now(),
                ex.getMessage()
        );

        return Response
                .status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
