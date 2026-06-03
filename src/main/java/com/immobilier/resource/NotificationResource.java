package com.immobilier.resource;

import com.immobilier.entity.User;
import com.immobilier.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Component
@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class NotificationResource {

    private final NotificationService notificationService;

    @GET
    @PreAuthorize("hasAnyRole('USER', 'BUYER', 'SELLER', 'AGENT', 'ADMIN')")
    public Response getNotifications() {
        return Response.ok(notificationService.getUserNotifications(currentUser())).build();
    }

    @GET
    @Path("/unread-count")
    @PreAuthorize("hasAnyRole('USER', 'BUYER', 'SELLER', 'AGENT', 'ADMIN')")
    public Response countUnread() {
        return Response.ok(Map.of("count", notificationService.countUnread(currentUser()))).build();
    }

    @PUT
    @Path("/{id}/read")
    @PreAuthorize("hasAnyRole('USER', 'BUYER', 'SELLER', 'AGENT', 'ADMIN')")
    public Response markRead(@PathParam("id") String id) {
        return Response.ok(notificationService.markAsRead(id, currentUser())).build();
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
