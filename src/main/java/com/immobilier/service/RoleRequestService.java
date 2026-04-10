package com.immobilier.service;

import com.immobilier.dto.RoleRequestDTO;
import com.immobilier.entity.RoleEnum;
import com.immobilier.entity.RoleRequest;
import com.immobilier.entity.User;
import com.immobilier.exception.ResourceNotFoundException;
import com.immobilier.repository.RoleRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleRequestService {

    private final RoleRequestRepository roleRequestRepository;
    private final UserService           userService;

    // ─────────────────────────────────────────────────────────────────────────
    // Utilisateur : soumettre une demande
    // ─────────────────────────────────────────────────────────────────────────

    public RoleRequestDTO submitRequest(String userId, RoleEnum requestedRole, String motivation) {
        User user = userService.getUserById(userId);

        // Vérifier qu'il n'y a pas déjà une demande PENDING
        roleRequestRepository.findByUserIdAndStatus(userId, RoleRequest.RequestStatus.PENDING)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                        "Vous avez déjà une demande en attente pour le rôle "
                        + existing.getRequestedRole().getDescription()
                        + ". Attendez qu'elle soit traitée."
                    );
                });

        // Vérifier que le rôle demandé est différent du rôle actuel
        if (user.getRole() == requestedRole) {
            throw new IllegalArgumentException(
                "Vous avez déjà le rôle " + requestedRole.getDescription()
            );
        }

        // Interdire de demander ADMIN directement
        if (requestedRole == RoleEnum.ROLE_ADMIN) {
            throw new IllegalArgumentException(
                "Vous ne pouvez pas demander le rôle Administrateur."
            );
        }

        RoleRequest request = RoleRequest.builder()
                .userId(userId)
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .currentRole(user.getRole())
                .requestedRole(requestedRole)
                .motivation(motivation)
                .status(RoleRequest.RequestStatus.PENDING)
                .build();

        RoleRequest saved = roleRequestRepository.save(request);
        log.info("Demande de rôle soumise : {} → {} (userId: {})",
                user.getRole(), requestedRole, userId);
        return convertToDTO(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilisateur : voir ses propres demandes
    // ─────────────────────────────────────────────────────────────────────────

    public List<RoleRequestDTO> getMyRequests(String userId) {
        return roleRequestRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin : voir toutes les demandes
    // ─────────────────────────────────────────────────────────────────────────

    public List<RoleRequestDTO> getAllRequests() {
        return roleRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<RoleRequestDTO> getPendingRequests() {
        return roleRequestRepository.findByStatusOrderByCreatedAtDesc(RoleRequest.RequestStatus.PENDING)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public long countPending() {
        return roleRequestRepository.countByStatus(RoleRequest.RequestStatus.PENDING);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin : approuver une demande
    // ─────────────────────────────────────────────────────────────────────────

    public RoleRequestDTO approveRequest(String requestId, String adminId, String adminComment) {
        RoleRequest request = getRequestById(requestId);

        if (request.getStatus() != RoleRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException("Cette demande a déjà été traitée.");
        }

        User admin = userService.getUserById(adminId);

        // Appliquer le nouveau rôle à l'utilisateur
        userService.changeUserRole(request.getUserId(), request.getRequestedRole());

        // Mettre à jour la demande
        request.setStatus(RoleRequest.RequestStatus.APPROVED);
        request.setAdminComment(adminComment);
        request.setProcessedBy(adminId);
        request.setProcessedByUsername(admin.getUsername());
        request.setProcessedAt(LocalDateTime.now());

        RoleRequest saved = roleRequestRepository.save(request);
        log.info("Demande {} approuvée par {} — rôle {} accordé à {}",
                requestId, admin.getUsername(), request.getRequestedRole(), request.getUsername());
        return convertToDTO(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin : rejeter une demande
    // ─────────────────────────────────────────────────────────────────────────

    public RoleRequestDTO rejectRequest(String requestId, String adminId, String adminComment) {
        RoleRequest request = getRequestById(requestId);

        if (request.getStatus() != RoleRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException("Cette demande a déjà été traitée.");
        }

        User admin = userService.getUserById(adminId);

        request.setStatus(RoleRequest.RequestStatus.REJECTED);
        request.setAdminComment(adminComment);
        request.setProcessedBy(adminId);
        request.setProcessedByUsername(admin.getUsername());
        request.setProcessedAt(LocalDateTime.now());

        RoleRequest saved = roleRequestRepository.save(request);
        log.info("Demande {} rejetée par {} — utilisateur : {}",
                requestId, admin.getUsername(), request.getUsername());
        return convertToDTO(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private RoleRequest getRequestById(String id) {
        return roleRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + id));
    }

    public RoleRequestDTO convertToDTO(RoleRequest r) {
        return RoleRequestDTO.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .username(r.getUsername())
                .email(r.getEmail())
                .fullName(r.getFullName())
                .currentRole(r.getCurrentRole())
                .requestedRole(r.getRequestedRole())
                .motivation(r.getMotivation())
                .status(r.getStatus())
                .adminComment(r.getAdminComment())
                .processedByUsername(r.getProcessedByUsername())
                .createdAt(r.getCreatedAt())
                .processedAt(r.getProcessedAt())
                .build();
    }
}
