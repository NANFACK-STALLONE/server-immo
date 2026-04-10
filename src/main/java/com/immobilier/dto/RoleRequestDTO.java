package com.immobilier.dto;

import com.immobilier.entity.RoleEnum;
import com.immobilier.entity.RoleRequest;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequestDTO {

    private String id;

    // Demandeur
    private String   userId;
    private String   username;
    private String   email;
    private String   fullName;
    private RoleEnum currentRole;

    // Demande
    private RoleEnum requestedRole;
    private String   motivation;

    // Traitement
    private RoleRequest.RequestStatus status;
    private String   adminComment;
    private String   processedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
