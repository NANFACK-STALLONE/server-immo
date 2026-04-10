package com.immobilier.repository;

import com.immobilier.entity.RoleRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRequestRepository extends MongoRepository<RoleRequest, String> {

    /** Toutes les demandes d'un utilisateur */
    List<RoleRequest> findByUserIdOrderByCreatedAtDesc(String userId);

    /** Toutes les demandes par statut (pour l'admin) */
    List<RoleRequest> findByStatusOrderByCreatedAtDesc(RoleRequest.RequestStatus status);

    /** Toutes les demandes triées par date (pour l'admin) */
    List<RoleRequest> findAllByOrderByCreatedAtDesc();

    /** Vérifier si une demande PENDING existe déjà pour cet utilisateur */
    Optional<RoleRequest> findByUserIdAndStatus(String userId, RoleRequest.RequestStatus status);

    /** Compter les demandes en attente (badge admin) */
    long countByStatus(RoleRequest.RequestStatus status);
}
