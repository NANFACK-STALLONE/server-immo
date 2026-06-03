package com.immobilier.resource;

import com.immobilier.dto.ChatMessageRequest;
import com.immobilier.dto.OfferRequest;
import com.immobilier.dto.VisitRequest;
import com.immobilier.entity.ChatMessage;
import com.immobilier.entity.Conversation;
import com.immobilier.entity.User;
import com.immobilier.service.InteractionService;
import lombok.RequiredArgsConstructor;
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

@Component
@Path("/api/interactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class InteractionResource {

    private final InteractionService interactionService;

    @POST
    @Path("/properties/{propertyId}/offer")
    @PreAuthorize("hasAnyRole('USER', 'BUYER', 'SELLER', 'AGENT', 'ADMIN')")
    public Response createOffer(@PathParam("propertyId") String propertyId, @Valid OfferRequest request) {
        Conversation conversation = interactionService.createOfferConversation(propertyId, request, currentUser());
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Conversation creee");
        body.put("conversation", conversation);
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    @POST
    @Path("/properties/{propertyId}/visit")
    @PreAuthorize("hasAnyRole('USER', 'BUYER', 'SELLER', 'AGENT', 'ADMIN')")
    public Response requestVisit(@PathParam("propertyId") String propertyId, @Valid VisitRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(interactionService.requestVisit(propertyId, request, currentUser()))
                .build();
    }

    @GET
    @Path("/conversations")
    @PreAuthorize("hasAnyRole('USER', 'BUYER', 'SELLER', 'AGENT', 'ADMIN')")
    public Response getConversations() {
        List<Conversation> conversations = interactionService.getUserConversations(currentUser());
        return Response.ok(conversations).build();
    }

    @GET
    @Path("/conversations/{conversationId}/messages")
    @PreAuthorize("hasAnyRole('USER', 'BUYER', 'SELLER', 'AGENT', 'ADMIN')")
    public Response getMessages(@PathParam("conversationId") String conversationId) {
        return Response.ok(interactionService.getMessages(conversationId, currentUser())).build();
    }

    @POST
    @Path("/conversations/{conversationId}/messages")
    @PreAuthorize("hasAnyRole('USER', 'BUYER', 'SELLER', 'AGENT', 'ADMIN')")
    public Response sendMessage(
            @PathParam("conversationId") String conversationId,
            @Valid ChatMessageRequest request) {
        ChatMessage message = interactionService.sendMessage(conversationId, currentUser(), request.getContent());
        return Response.status(Response.Status.CREATED).entity(message).build();
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
