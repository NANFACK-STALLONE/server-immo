package com.immobilier.resource;

import com.immobilier.dto.LoginRequest;
import com.immobilier.dto.LoginResponse;
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
 * Resource JAX-RS pour l'authentification.
 *
 * Annotations JAX-RS utilisées (remplacent Spring MVC) :
 *   @Path          ← @RequestMapping
 *   @POST / @GET   ← @PostMapping / @GetMapping
 *   @QueryParam    ← @RequestParam
 *   @HeaderParam   ← @RequestHeader
 *   @Consumes      ← Content-Type attendu
 *   @Produces      ← Content-Type retourné
 *   Response       ← ResponseEntity
 */
@Component
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Slf4j
public class AuthResource {

    private final AuthService authService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/auth/login
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Authentifie un utilisateur et retourne les tokens JWT.
     *
     * Accès : Public
     *
     * @param loginRequest  Body JSON { "email": "...", "password": "..." }
     * @return 200 OK avec accessToken + refreshToken
     *         401 si identifiants incorrects
     */
    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest loginRequest) {
        log.info("Requête de connexion pour: {}", loginRequest.getEmail());

        LoginResponse response = authService.login(loginRequest);

        return Response.ok(response).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/auth/register
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Crée un nouveau compte utilisateur.
     *
     * Accès : Public
     *
     * @param username  Nom d'utilisateur unique
     * @param email     Email unique
     * @param password  Mot de passe
     * @param fullName  Nom complet
     * @return 201 Created avec les infos du compte créé
     *         400 si username ou email déjà utilisé
     */
    @POST
    @Path("/register")
    public Response register(
            @QueryParam("username") String username,
            @QueryParam("email")    String email,
            @QueryParam("password") String password,
            @QueryParam("fullName") String fullName) {

        log.info("Nouvelle demande d'enregistrement pour: {}", email);

        User newUser = authService.register(username, email, password, fullName);

        Map<String, Object> body = new HashMap<>();
        body.put("message",  "Enregistrement réussi");
        body.put("userId",   newUser.getId());
        body.put("username", newUser.getUsername());
        body.put("email",    newUser.getEmail());

        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/auth/refresh
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Génère un nouvel access token à partir d'un refresh token valide.
     *
     * Accès : Public
     *
     * @param refreshToken  Refresh token JWT en query param
     * @return 200 OK avec les nouveaux tokens
     *         400 si le refresh token est invalide ou expiré
     */
    @POST
    @Path("/refresh")
    public Response refreshToken(@QueryParam("refreshToken") String refreshToken) {
        log.info("Demande de rafraîchissement de token");

        LoginResponse response = authService.refreshToken(refreshToken);

        return Response.ok(response).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/auth/validate
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Vérifie si un access token JWT est valide.
     *
     * Accès : Public
     *
     * @param authHeader  Header Authorization: Bearer <token>
     * @return 200 { "valid": true }  ou  401 { "valid": false }
     */
    @GET
    @Path("/validate")
    public Response validateToken(@HeaderParam("Authorization") String authHeader) {
        log.info("Validation de token");

        // Extraire le token du header "Bearer <token>"
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

    /**
     * Health check — vérifie que l'API est en ligne.
     *
     * Accès : Public
     *
     * @return 200 { "status": "UP", ... }
     */
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
