package com.immobilier.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Entité MongoDB — Demande de changement de rôle.
 *
 * Un utilisateur (ROLE_USER) soumet une demande pour obtenir un rôle
 * supérieur (SELLER, AGENT, BUYER). L'admin peut ensuite approuver ou
 * rejeter la demande avec un commentaire optionnel.
 */
@Document(collection = "role_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequest {

    @Id
    private String id;

    // ── Demandeur ──────────────────────────────────────────────────────────
    private String   userId;
    private String   username;
    private String   email;
    private String   fullName;
    private RoleEnum currentRole;

    // ── Demande ────────────────────────────────────────────────────────────
    private RoleEnum requestedRole;

    /** Motivation / message du demandeur (optionnel) */
    private String motivation;

    // ── Traitement ─────────────────────────────────────────────────────────
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    /** Commentaire de l'admin lors de l'approbation ou du rejet (optionnel) */
    private String adminComment;

    /** ID de l'admin qui a traité la demande */
    private String processedBy;

    /** Username de l'admin qui a traité la demande */
    private String processedByUsername;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    // ── Enum statut ────────────────────────────────────────────────────────
    public enum RequestStatus {
        PENDING("En attente"),
        APPROVED("Approuvée"),
        REJECTED("Rejetée");

        private final String label;

        RequestStatus(String label) { this.label = label; }

        public String getLabel() { return label; }
    }
}
