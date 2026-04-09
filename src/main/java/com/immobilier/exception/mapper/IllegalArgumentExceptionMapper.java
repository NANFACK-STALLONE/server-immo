package com.immobilier.exception.mapper;

import com.immobilier.exception.ApiError;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.time.LocalDateTime;

/**
 * ExceptionMapper JAX-RS pour IllegalArgumentException.
 *
 * Retourne 400 Bad Request quand un argument passé au service est invalide.
 * Exemple : tentative de changer le statut avec une valeur inexistante.
 */
@Provider
@Slf4j
public class IllegalArgumentExceptionMapper
        implements ExceptionMapper<IllegalArgumentException> {

    @Override
    public Response toResponse(IllegalArgumentException ex) {
        log.warn("Argument invalide: {}", ex.getMessage());

        ApiError error = new ApiError(
                400,
                "Argument invalide",
                ex.getMessage(),
                "",
                LocalDateTime.now(),
                null
        );

        return Response
                .status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
