package com.immobilier.exception.mapper;

import com.immobilier.exception.ApiError;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * ExceptionMapper JAX-RS pour ConstraintViolationException (Bean Validation).
 *
 * Remplace handleMethodArgumentNotValid() de Spring MVC.
 *
 * JAX-RS / Jersey utilise ConstraintViolationException (JSR-380) au lieu de
 * MethodArgumentNotValidException (Spring MVC) quand la validation @Valid échoue.
 *
 * Retourne 400 Bad Request avec le détail des champs invalides.
 */
@Provider
@Slf4j
public class ValidationExceptionMapper
        implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException ex) {
        // Concat tous les messages de violation sous forme "champ: message"
        String details = ex.getConstraintViolations()
                .stream()
                .map(v -> {
                    // Le path contient "methodName.paramName.fieldName"
                    // On extrait seulement le dernier segment (le nom du champ)
                    String path = v.getPropertyPath().toString();
                    String field = path.contains(".")
                            ? path.substring(path.lastIndexOf('.') + 1)
                            : path;
                    return field + ": " + v.getMessage();
                })
                .collect(Collectors.joining(", "));

        log.warn("Erreur de validation: {}", details);

        ApiError error = new ApiError(
                400,
                "Erreur de validation",
                "Les données envoyées sont invalides",
                "",
                LocalDateTime.now(),
                details
        );

        return Response
                .status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
