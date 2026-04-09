package com.immobilier.exception.mapper;

import com.immobilier.exception.ApiError;
import com.immobilier.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.time.LocalDateTime;

/**
 * ExceptionMapper JAX-RS pour ResourceNotFoundException.
 *
 * Remplace l'équivalent Spring MVC :
 *   @ExceptionHandler(ResourceNotFoundException.class)
 *   dans GlobalExceptionHandler.java
 *
 * @Provider  → déclare ce mapper auprès du runtime JAX-RS (Jersey)
 * ExceptionMapper<T> → interface JAX-RS (javax.ws.rs.ext.ExceptionMapper)
 */
@Provider
@Slf4j
public class ResourceNotFoundExceptionMapper
        implements ExceptionMapper<ResourceNotFoundException> {

    @Override
    public Response toResponse(ResourceNotFoundException ex) {
        log.warn("Ressource non trouvée: {}", ex.getMessage());

        ApiError error = new ApiError(
                404,
                "Ressource non trouvée",
                ex.getMessage(),
                "",
                LocalDateTime.now(),
                null
        );

        return Response
                .status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
