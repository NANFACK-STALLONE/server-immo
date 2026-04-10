package com.immobilier.resource;

import com.immobilier.dto.RoleRequestDTO;
import com.immobilier.entity.RoleEnum;
import com.immobilier.entity.User;
import com.immobilier.service.RoleRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource JAX-RS — Demandes de changement de rôle.
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │ Utilisateur                                                  │
 * │  POST /api/role-requests          — soumettre une demande   │
 * │  GET  /api/role-requests/mine     — mes demandes            │
 * ├──────────────────────────────────────────────────────────────┤
 * │ Admin                                                        │
 * │  GET  /api/role-requests          — toutes les demandes     │
 * │  GET  /api/role-requests/pending  — demandes en attente     │
 * │  GET  /api/role-requests/count    — nb demandes en attente  │
 * │  PUT  /api/role-requests/{id}/approve — approuver           │
 * │  PUT  /api/role-requests/{id}/reject  — rejeter             │
 * └──────────────────────────────────────────────────────────────┘
 */
@Component
@Path("/api/role-requests")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Slf4j
public class RoleRequestResource {

    private final RoleRequestService roleRequestService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/role-requests   — Soumettre une demande (utilisateur connecté)
    // ─────────────────────────────────────────────────────────────────────────
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @PreAuthorize("isAuthenticated()")
    public Response submitRequest(
            @QueryParam("requestedRole") String requestedRole,
            @QueryParam("motivation")    String motivation) {

        String userId = getCurrentUserId();
        RoleEnum role = RoleEnum.valueOf(requestedRole.toUpperCase());

        RoleRequestDTO dto = roleRequestService.submitRequest(userId, role, motivation);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Demande soumise avec succès. L'administrateur va l'examiner.");
        body.put("request", dto);
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/role-requests/mine   — Mes propres demandes
    // ─────────────────────────────────────────────────────────────────────────
    @GET
    @Path("/mine")
    @PreAuthorize("isAuthenticated()")
    public Response getMyRequests() {
        String userId = getCurrentUserId();
        List<RoleRequestDTO> list = roleRequestService.getMyRequests(userId);
        return Response.ok(list).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/role-requests   — Toutes les demandes (admin)
    // ─────────────────────────────────────────────────────────────────────────
    @GET
    @PreAuthorize("hasRole('ADMIN')")
    public Response getAllRequests() {
        List<RoleRequestDTO> list = roleRequestService.getAllRequests();
        return Response.ok(list).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/role-requests/pending   — Demandes en attente (admin)
    // ─────────────────────────────────────────────────────────────────────────
    @GET
    @Path("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Response getPendingRequests() {
        List<RoleRequestDTO> list = roleRequestService.getPendingRequests();
        return Response.ok(list).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/role-requests/count   — Nombre de demandes en attente (admin)
    // ─────────────────────────────────────────────────────────────────────────
    @GET
    @Path("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public Response countPending() {
        Map<String, Object> body = new HashMap<>();
        body.put("pending", roleRequestService.countPending());
        return Response.ok(body).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/role-requests/{id}/approve   — Approuver (admin)
    // ─────────────────────────────────────────────────────────────────────────
    @PUT
    @Path("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Response approveRequest(
            @PathParam("id")             String id,
            @QueryParam("adminComment")  String adminComment) {

        String adminId = getCurrentUserId();
        RoleRequestDTO dto = roleRequestService.approveRequest(id, adminId, adminComment);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Demande approuvée. Le rôle a été attribué.");
        body.put("request", dto);
        return Response.ok(body).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/role-requests/{id}/reject   — Rejeter (admin)
    // ─────────────────────────────────────────────────────────────────────────
    @PUT
    @Path("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Response rejectRequest(
            @PathParam("id")             String id,
            @QueryParam("adminComment")  String adminComment) {

        String adminId = getCurrentUserId();
        RoleRequestDTO dto = roleRequestService.rejectRequest(id, adminId, adminComment);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Demande rejetée.");
        body.put("request", dto);
        return Response.ok(body).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitaire privé
    // ─────────────────────────────────────────────────────────────────────────
    private String getCurrentUserId() {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return user.getId();
    }
}
