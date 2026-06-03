package com.immobilier.resource;

import com.immobilier.dto.ChangePasswordRequest;
import com.immobilier.dto.UserDTO;
import com.immobilier.entity.User;
import com.immobilier.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource JAX-RS pour la gestion des utilisateurs.
 *
 * Correspondance des annotations (Spring MVC → JAX-RS) :
 *   @RestController          →  @Component + @Path
 *   @RequestMapping          →  @Path (au niveau classe)
 *   @GetMapping              →  @GET  + @Path (au niveau méthode)
 *   @PostMapping             →  @POST + @Path
 *   @PutMapping              →  @PUT  + @Path
 *   @DeleteMapping           →  @DELETE + @Path
 *   @PathVariable            →  @PathParam
 *   @RequestParam            →  @QueryParam
 *   @RequestBody             →  paramètre direct (Jersey désérialise le body JSON)
 *   ResponseEntity<T>        →  Response (javax.ws.rs.core.Response)
 *   ResponseEntity.ok(x)     →  Response.ok(x).build()
 *   ResponseEntity.status(n) →  Response.status(n).entity(x).build()
 *
 * L'objet Authentication de Spring Security est récupéré via
 * SecurityContextHolder (le filtre JWT l'a déjà placé avant Jersey).
 */
@Component
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Slf4j
public class UserResource {

    private final UserService userService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/users/profile
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retourne le profil de l'utilisateur actuellement connecté.
     *
     * Accès : tous les rôles authentifiés
     */
    @GET
    @Path("/profile")
    @PreAuthorize("hasAnyRole('USER', 'AGENT', 'BUYER', 'SELLER', 'ADMIN')")
    public Response getCurrentUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName());
        return Response.ok(userService.convertToDTO(user)).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/users/profile
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Modifie le profil de l'utilisateur connecté.
     *
     * @param userDTO  Body JSON contenant les champs à mettre à jour
     */
    @PUT
    @Path("/profile")
    @PreAuthorize("hasAnyRole('USER', 'AGENT', 'BUYER', 'SELLER', 'ADMIN')")
    public Response updateProfile(@Valid UserDTO userDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user    = userService.getUserByUsername(auth.getName());
        User updated = userService.updateUser(user.getId(), userDTO);
        return Response.ok(userService.convertToDTO(updated)).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/users/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retourne un utilisateur par son ID MongoDB.
     *
     * @param id  ID MongoDB de l'utilisateur  (ex: 6073f6a9e5f3a12b3c4d5e6f)
     */
    @GET
    @Path("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'AGENT', 'BUYER', 'SELLER', 'ADMIN')")
    public Response getUserById(@PathParam("id") String id) {
        User user = userService.getUserById(id);
        return Response.ok(userService.convertToDTO(user)).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/users
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retourne la liste de tous les utilisateurs.
     *
     * Accès : ROLE_ADMIN uniquement
     */
    @GET
    @PreAuthorize("hasRole('ADMIN')")
    public Response getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return Response.ok(users).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/users/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Modifie un utilisateur (admin seulement).
     *
     * @param id       ID MongoDB de l'utilisateur
     * @param userDTO  Body JSON avec les champs à modifier
     */
    @PUT
    @Path("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Response updateUser(@PathParam("id") String id,
                               @Valid UserDTO userDTO) {
        User updated = userService.updateUser(id, userDTO);
        return Response.ok(userService.convertToDTO(updated)).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/users/change-password
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Permet à l'utilisateur connecté de changer son mot de passe.
     *
     * @param oldPassword  Ancien mot de passe
     * @param newPassword  Nouveau mot de passe
     */
    @POST
    @Path("/change-password")
    @PreAuthorize("hasAnyRole('USER', 'AGENT', 'BUYER', 'SELLER', 'ADMIN')")
    public Response changePassword(@Valid ChangePasswordRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName());
        userService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Mot de passe changé avec succès");
        return Response.ok(body).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/users/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Supprime définitivement un utilisateur.
     *
     * Accès : ROLE_ADMIN uniquement
     *
     * @param id  ID MongoDB de l'utilisateur
     */
    @DELETE
    @Path("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Response deleteUser(@PathParam("id") String id) {
        userService.deleteUser(id);
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Utilisateur supprimé avec succès");
        return Response.ok(body).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/users/{id}/role   — changer le rôle d'un utilisateur
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Change le rôle d'un utilisateur.
     *
     * Accès : ROLE_ADMIN uniquement
     *
     * @param id    ID MongoDB de l'utilisateur
     * @param role  Nouveau rôle : ROLE_USER | ROLE_BUYER | ROLE_SELLER | ROLE_AGENT | ROLE_ADMIN
     */
    @PUT
    @Path("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public Response changeUserRole(
            @PathParam("id")    String id,
            @QueryParam("role") String role) {

        com.immobilier.entity.RoleEnum newRole =
                com.immobilier.entity.RoleEnum.valueOf(role.toUpperCase());
        User updated = userService.changeUserRole(id, newRole);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Rôle mis à jour avec succès");
        body.put("user",    userService.convertToDTO(updated));
        return Response.ok(body).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/users/me/become-buyer  — passer directement au rôle ACHETEUR
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Permet à un utilisateur (ROLE_USER) de passer directement au rôle ROLE_BUYER
     * sans avoir besoin d'une validation admin.
     *
     * Accès : tout utilisateur authentifié (sauf si déjà BUYER ou rôle supérieur)
     */
    @PUT
    @Path("/me/become-buyer")
    @PreAuthorize("hasAnyRole('USER', 'BUYER', 'AGENT', 'BUYER', 'SELLER', 'ADMIN')")
    public Response becomeBuyer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName());

        // Autoriser uniquement si rôle actuel est ROLE_USER
        if (user.getRole() != com.immobilier.entity.RoleEnum.ROLE_USER) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "Vous avez déjà un rôle : " + user.getRole().name());
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        User updated = userService.changeUserRole(user.getId(), com.immobilier.entity.RoleEnum.ROLE_BUYER);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Félicitations ! Vous êtes maintenant Acheteur.");
        body.put("user",    userService.convertToDTO(updated));
        return Response.ok(body).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/users/{id}/disable
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Désactive le compte d'un utilisateur (isActive = false).
     *
     * Accès : ROLE_ADMIN uniquement
     */
    @PUT
    @Path("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public Response disableUser(@PathParam("id") String id) {
        User user = userService.toggleUserStatus(id, false);
        return Response.ok(userService.convertToDTO(user)).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/users/{id}/enable
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Active le compte d'un utilisateur (isActive = true).
     *
     * Accès : ROLE_ADMIN uniquement
     */
    @PUT
    @Path("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public Response enableUser(@PathParam("id") String id) {
        User user = userService.toggleUserStatus(id, true);
        return Response.ok(userService.convertToDTO(user)).build();
    }
}
