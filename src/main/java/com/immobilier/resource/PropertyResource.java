package com.immobilier.resource;

import com.immobilier.dto.PageResponse;
import com.immobilier.dto.PropertyDTO;
import com.immobilier.entity.Property;
import com.immobilier.entity.User;
import com.immobilier.service.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resource JAX-RS pour la gestion des propriétés immobilières.
 *
 * Table de correspondance complète Spring MVC → JAX-RS :
 * ┌────────────────────────────┬───────────────────────────────────────┐
 * │ Spring MVC                 │ JAX-RS (Jersey)                       │
 * ├────────────────────────────┼───────────────────────────────────────┤
 * │ @RestController            │ @Component + @Path                    │
 * │ @RequestMapping("/path")   │ @Path("/path")  sur la classe         │
 * │ @GetMapping("/sub")        │ @GET + @Path("/sub")  sur la méthode  │
 * │ @PostMapping               │ @POST                                 │
 * │ @PutMapping("/sub")        │ @PUT + @Path("/sub")                  │
 * │ @DeleteMapping("/sub")     │ @DELETE + @Path("/sub")               │
 * │ @PathVariable String id    │ @PathParam("id") String id            │
 * │ @RequestParam String x     │ @QueryParam("x") String x             │
 * │ @RequestParam(def="v")     │ @QueryParam("x") @DefaultValue("v")   │
 * │ @RequestBody DTO dto       │ DTO dto  (pas d'annotation, body JSON) │
 * │ ResponseEntity<T>          │ Response                              │
 * │ ResponseEntity.ok(body)    │ Response.ok(body).build()             │
 * │ ResponseEntity.status(201) │ Response.status(201).entity(x).build()│
 * │ @CrossOrigin               │ géré dans SecurityConfig (CORS)       │
 * │ Authentication auth        │ SecurityContextHolder.getContext()...  │
 * └────────────────────────────┴───────────────────────────────────────┘
 */
@Component
@Path("/api/properties")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Slf4j
public class PropertyResource {

    private final PropertyService propertyService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/properties/public
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Liste toutes les propriétés publiées et disponibles (paginé).
     *
     * Accès : Public (sans token)
     *
     * @param page  Numéro de page (défaut 0)
     * @param size  Taille de page  (défaut 10)
     */
    @GET
    @Path("/public")
    public Response getPublishedProperties(
            @QueryParam("page") @DefaultValue("0")  int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Property> pageResult = propertyService.getAllPublishedProperties(pageable);

        // On convertit Page<Property> → PageResponse<PropertyDTO>
        // (PageResponse est notre wrapper JAX-RS, pas Spring-spécifique)
        PageResponse<PropertyDTO> response =
                PageResponse.of(pageResult, propertyService::convertToDTO);

        return Response.ok(response).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/properties/search
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Recherche des propriétés avec filtres dynamiques (tous optionnels).
     *
     * Accès : Public (sans token)
     *
     * @param city      Filtrer par ville
     * @param minPrice  Prix minimum (FCFA)
     * @param maxPrice  Prix maximum (FCFA)
     * @param bedrooms  Nombre minimum de chambres
     * @param page      Numéro de page
     * @param size      Taille de page
     */
    @GET
    @Path("/search")
    public Response searchProperties(
            @QueryParam("city")         String  city,
            @QueryParam("minPrice")     Double  minPrice,
            @QueryParam("maxPrice")     Double  maxPrice,
            @QueryParam("bedrooms")     Integer bedrooms,
            @QueryParam("propertyType") String  propertyType,
            @QueryParam("page") @DefaultValue("0")  int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Property> pageResult =
                propertyService.searchProperties(city, minPrice, maxPrice, bedrooms, propertyType, pageable);

        PageResponse<PropertyDTO> response =
                PageResponse.of(pageResult, propertyService::convertToDTO);

        return Response.ok(response).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/properties/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retourne le détail d'une propriété par son ID MongoDB.
     *
     * Accès : Public (sans token)
     *
     * @param id  ID MongoDB de la propriété
     */
    @GET
    @Path("/{id}")
    public Response getPropertyById(@PathParam("id") String id) {
        Property property = propertyService.getPropertyById(id);
        return Response.ok(propertyService.convertToDTO(property)).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/properties
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Crée une nouvelle propriété.
     *
     * Accès : ROLE_SELLER, ROLE_AGENT, ROLE_ADMIN
     *
     * @param propertyDTO  Body JSON décrivant la propriété à créer
     * @return 201 Created avec le PropertyDTO créé
     */
    @POST
    @PreAuthorize("hasAnyRole('SELLER', 'AGENT', 'ADMIN')")
    public Response createProperty(@Valid PropertyDTO propertyDTO) {
        String userId = extractCurrentUserId();
        Property property = propertyService.createProperty(propertyDTO, userId);
        return Response
                .status(Response.Status.CREATED)
                .entity(propertyService.convertToDTO(property))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/properties/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Modifie une propriété existante (propriétaire uniquement).
     *
     * Accès : ROLE_SELLER, ROLE_AGENT, ROLE_ADMIN
     *
     * @param id           ID MongoDB de la propriété
     * @param propertyDTO  Body JSON avec les champs à modifier
     */
    @PUT
    @Path("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'AGENT', 'ADMIN')")
    public Response updateProperty(@PathParam("id") String id,
                                   @Valid PropertyDTO propertyDTO) {
        String userId = extractCurrentUserId();
        Property updated = propertyService.updateProperty(id, propertyDTO, userId);
        return Response.ok(propertyService.convertToDTO(updated)).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/properties/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Supprime une propriété (propriétaire uniquement).
     *
     * Accès : ROLE_SELLER, ROLE_AGENT, ROLE_ADMIN
     *
     * @param id  ID MongoDB de la propriété
     */
    @DELETE
    @Path("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'AGENT', 'ADMIN')")
    public Response deleteProperty(@PathParam("id") String id) {
        String userId = extractCurrentUserId();
        propertyService.deleteProperty(id, userId);
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Propriété supprimée avec succès");
        return Response.ok(body).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/properties/owner/list
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retourne toutes les propriétés appartenant à l'utilisateur connecté.
     *
     * Accès : ROLE_SELLER, ROLE_AGENT, ROLE_ADMIN
     */
    @GET
    @Path("/owner/list")
    @PreAuthorize("hasAnyRole('SELLER', 'AGENT', 'ADMIN')")
    public Response getUserProperties() {
        String userId = extractCurrentUserId();
        List<PropertyDTO> dtos = propertyService.getPropertiesByOwner(userId)
                .stream()
                .map(propertyService::convertToDTO)
                .collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/properties/{id}/status
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Change le statut d'une propriété.
     *
     * Accès : ROLE_SELLER, ROLE_AGENT, ROLE_ADMIN (propriétaire uniquement)
     *
     * @param id      ID MongoDB de la propriété
     * @param status  Nouveau statut : AVAILABLE | RESERVED | SOLD | RENT
     */
    @PUT
    @Path("/{id}/status")
    @PreAuthorize("hasAnyRole('SELLER', 'AGENT', 'ADMIN')")
    public Response updatePropertyStatus(
            @PathParam("id")        String id,
            @QueryParam("status")   String status) {

        String userId = extractCurrentUserId();
        Property.PropertyStatus propertyStatus =
                Property.PropertyStatus.valueOf(status.toUpperCase());
        Property updated = propertyService.updatePropertyStatus(id, propertyStatus, userId);
        return Response.ok(propertyService.convertToDTO(updated)).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/properties/{id}/assign-agent
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Assigne un agent immobilier à une propriété.
     *
     * Accès : ROLE_SELLER, ROLE_ADMIN (propriétaire uniquement)
     *
     * @param id       ID MongoDB de la propriété
     * @param agentId  ID MongoDB de l'agent à assigner
     */
    @PUT
    @Path("/{id}/assign-agent")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public Response assignAgent(
            @PathParam("id")       String id,
            @QueryParam("agentId") String agentId) {

        String userId = extractCurrentUserId();
        Property updated = propertyService.assignAgent(id, agentId, userId);
        return Response.ok(propertyService.convertToDTO(updated)).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/properties/{id}/publish
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Publie ou dépublie une propriété.
     *
     * Accès : ROLE_SELLER, ROLE_AGENT, ROLE_ADMIN (propriétaire uniquement)
     *
     * @param id       ID MongoDB de la propriété
     * @param publish  true pour publier, false pour dépublier
     */
    @PUT
    @Path("/{id}/publish")
    @PreAuthorize("hasAnyRole('SELLER', 'AGENT', 'ADMIN')")
    public Response publishProperty(
            @PathParam("id")        String  id,
            @QueryParam("publish")  Boolean publish) {

        String userId = extractCurrentUserId();
        Property updated = propertyService.publishProperty(id, publish, userId);
        return Response.ok(propertyService.convertToDTO(updated)).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitaire privé
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Récupère l'ID de l'utilisateur connecté depuis le SecurityContext Spring.
     *
     * Contrairement à Spring MVC où on injectait Authentication dans le paramètre,
     * ici on utilise SecurityContextHolder car JAX-RS ne connaît pas le type
     * Authentication de Spring Security.
     */
    private String extractCurrentUserId() {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return user.getId();
    }
}
