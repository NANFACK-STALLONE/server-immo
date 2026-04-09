package com.immobilier.repository;

import com.immobilier.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends MongoRepository<Property, String> {

    List<Property> findByOwnerId(String ownerId);

    List<Property> findByAgentId(String agentId);

    Page<Property> findByCity(String city, Pageable pageable);

    Page<Property> findByPropertyType(Property.PropertyType propertyType, Pageable pageable);

    Page<Property> findByStatus(Property.PropertyStatus status, Pageable pageable);

    @Query("{ 'isPublished': true, 'status': 'AVAILABLE' }")
    Page<Property> findAllPublishedAndAvailable(Pageable pageable);

    List<Property> findByAgentIdAndStatus(String agentId, Property.PropertyStatus status);

    Optional<Property> findByIdAndOwnerId(String propertyId, String ownerId);

    Optional<Property> findByIdAndAgentId(String propertyId, String agentId);

    long countByOwnerId(String ownerId);

    long countByAgentId(String agentId);
}
