package com.immobilier.exception.mapper;

import com.immobilier.exception.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.time.LocalDateTime;

/**
 * ExceptionMapper JAX-RS pour BadCredentialsException (Spring Security).
 *
 * Retourne 401 Unauthorized quand l'email ou le mot de passe est incorrect.
 */
@Provider
@Slf4j
public class BadCredentialsExceptionMapper
        implements ExceptionMapper<BadCredentialsException> {

    @Override
    public Response toResponse(BadCredentialsException ex) {
        log.warn("Tentative d'authentification échouée");

        ApiError error = new ApiError(
                401,
                "Authentification échouée",
                ex.getMessage(),
                "",
                LocalDateTime.now(),
                null
        );

        return Response
                .status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
