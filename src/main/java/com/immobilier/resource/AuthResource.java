package com.immobilier.resource;

import com.immobilier.dto.LoginRequest;
import com.immobilier.dto.LoginResponse;
import com.immobilier.dto.RefreshTokenRequest;
import com.immobilier.dto.RegisterRequest;
import com.immobilier.entity.User;
import com.immobilier.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Resource JAX-RS — Authentification
 *
 * @Consumes est placé uniquement sur les méthodes qui ont un body JSON.
 * Les méthodes qui utilisent @QueryParam n'ont pas de body → pas de @Consumes
 * au niveau classe pour éviter les rejets 415 Unsupported Media Type.
 */
@Component
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Slf4j
public class AuthResource {

    private final AuthService authService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/auth/login   — body JSON
    // ─────────────────────────────────────────────────────────────────────────
    @POST
    @Path("/login")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.WILDCARD})
    public Response login(LoginRequest loginRequest) {
        log.info("Requête de connexion pour: {}", loginRequest.getEmail());
        LoginResponse response = authService.login(loginRequest);
        return Response.ok(response).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/auth/register   — body JSON
    // ─────────────────────────────────────────────────────────────────────────
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(@Valid RegisterRequest request) {

        log.info("Nouvelle demande d'enregistrement pour: {}", request.getEmail());
        User newUser = authService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFullName()
        );

        Map<String, Object> body = new HashMap<>();
        body.put("message",  "Enregistrement réussi");
        body.put("userId",   newUser.getId());
        body.put("username", newUser.getUsername());
        body.put("email",    newUser.getEmail());

        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/auth/refresh   — paramètre dans l'URL (@QueryParam)
    // ─────────────────────────────────────────────────────────────────────────
    @POST
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response refreshToken(@Valid RefreshTokenRequest request) {
        log.info("Demande de rafraîchissement de token");
        LoginResponse response = authService.refreshToken(request.getRefreshToken());
        return Response.ok(response).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/auth/validate   — token dans le header
    // ─────────────────────────────────────────────────────────────────────────
    @GET
    @Path("/validate")
    public Response validateToken(@HeaderParam("Authorization") String authHeader) {
        log.info("Validation de token");
        String token = authHeader != null
                ? authHeader.replace("Bearer ", "").trim()
                : "";

        boolean isValid = authService.validateAccessToken(token);

        Map<String, Object> body = new HashMap<>();
        body.put("valid",   isValid);
        body.put("message", isValid ? "Token valide" : "Token invalide ou expiré");

        return isValid
                ? Response.ok(body).build()
                : Response.status(Response.Status.UNAUTHORIZED).entity(body).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/auth/health
    // ─────────────────────────────────────────────────────────────────────────
    @GET
    @Path("/health")
    public Response health() {
        Map<String, Object> body = new HashMap<>();
        body.put("status",    "UP");
        body.put("message",   "API Immobilier est en ligne");
        body.put("timestamp", System.currentTimeMillis());
        return Response.ok(body).build();
    }
}
