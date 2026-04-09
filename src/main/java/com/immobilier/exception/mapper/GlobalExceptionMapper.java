package com.immobilier.exception.mapper;

import com.immobilier.exception.ApiError;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.time.LocalDateTime;

/**
 * ExceptionMapper JAX-RS générique (catch-all).
 *
 * Intercepte toutes les exceptions non gérées par les mappers spécifiques.
 * Retourne 500 Internal Server Error.
 *
 * Important : les WebApplicationException (404, 405…) lancées par Jersey
 * lui-même sont renvoyées telles quelles pour ne pas masquer les erreurs
 * natives de routage JAX-RS.
 */
@Provider
@Slf4j
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception ex) {

        // Laisser passer les exceptions JAX-RS natives (404 route inconnue, etc.)
        if (ex instanceof WebApplicationException) {
            return ((WebApplicationException) ex).getResponse();
        }

        log.error("Erreur interne non gérée: ", ex);

        ApiError error = new ApiError(
                500,
                "Erreur interne du serveur",
                ex.getMessage() != null ? ex.getMessage() : "Une erreur inattendue s'est produite",
                "",
                LocalDateTime.now(),
                null
        );

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
