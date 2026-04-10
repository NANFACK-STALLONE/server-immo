package com.immobilier.service;

import com.immobilier.dto.PropertyDTO;
import com.immobilier.entity.Property;
import com.immobilier.entity.User;
import com.immobilier.exception.ResourceNotFoundException;
import com.immobilier.repository.PropertyRepository;
import com.immobilier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public Property createProperty(PropertyDTO propertyDTO, String ownerId) {
        log.info("Création d'une nouvelle propriété par le propriétaire: {}", ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Propriétaire non trouvé"));

        Property property = Property.builder()
                .title(propertyDTO.getTitle())
                .description(propertyDTO.getDescription())
                .price(propertyDTO.getPrice())
                .area(propertyDTO.getArea())
                .bedrooms(propertyDTO.getBedrooms())
                .bathrooms(propertyDTO.getBathrooms())
                .propertyType(propertyDTO.getPropertyType())
                .city(propertyDTO.getCity())
                .neighborhood(propertyDTO.getNeighborhood())
                .address(propertyDTO.getAddress())
                .latitude(propertyDTO.getLatitude())
                .longitude(propertyDTO.getLongitude())
                .ownerId(owner.getId())
                .ownerName(owner.getFullName())
                .isPublished(propertyDTO.getIsPublished() != null ? propertyDTO.getIsPublished() : true)
                .status(Property.PropertyStatus.AVAILABLE)
                .features(propertyDTO.getFeatures() != null ? propertyDTO.getFeatures() : new ArrayList<>())
                .build();

        return propertyRepository.save(property);
    }

    public Property getPropertyById(String id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propriété non trouvée avec l'ID: " + id));
    }

    public Page<Property> getAllPublishedProperties(Pageable pageable) {
        return propertyRepository.findAllPublishedAndAvailable(pageable);
    }

    public List<Property> getPropertiesByOwner(String ownerId) {
        return propertyRepository.findByOwnerId(ownerId);
    }

    public List<Property> getPropertiesByAgent(String agentId) {
        return propertyRepository.findByAgentId(agentId);
    }

    public Page<Property> searchProperties(String city, Double minPrice, Double maxPrice,
                                            Integer bedrooms, String propertyType, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("isPublished").is(true));

        if (city != null && !city.isBlank()) {
            // Recherche insensible à la casse sur la ville
            criteriaList.add(Criteria.where("city").regex("^" + city.trim() + "$", "i"));
        }
        if (minPrice != null && maxPrice != null) {
            criteriaList.add(Criteria.where("price").gte(minPrice).lte(maxPrice));
        } else if (minPrice != null) {
            criteriaList.add(Criteria.where("price").gte(minPrice));
        } else if (maxPrice != null) {
            criteriaList.add(Criteria.where("price").lte(maxPrice));
        }
        if (bedrooms != null) {
            criteriaList.add(Criteria.where("bedrooms").gte(bedrooms));
        }
        if (propertyType != null && !propertyType.isBlank()) {
            criteriaList.add(Criteria.where("propertyType").is(
                    Property.PropertyType.valueOf(propertyType.toUpperCase())
            ));
        }

        Query query = new Query(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        long count = mongoTemplate.count(query, Property.class);
        query.with(pageable);
        List<Property> properties = mongoTemplate.find(query, Property.class);
        return new PageImpl<>(properties, pageable, count);
    }

    public Property updateProperty(String id, PropertyDTO propertyDTO, String ownerId) {
        Property property = getPropertyById(id);

        if (!property.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Vous n'avez pas le droit de modifier cette propriété");
        }

        log.info("Mise à jour de la propriété: {}", id);
        property.setTitle(propertyDTO.getTitle());
        property.setDescription(propertyDTO.getDescription());
        property.setPrice(propertyDTO.getPrice());
        property.setArea(propertyDTO.getArea());
        property.setBedrooms(propertyDTO.getBedrooms());
        property.setBathrooms(propertyDTO.getBathrooms());
        property.setPropertyType(propertyDTO.getPropertyType());
        property.setCity(propertyDTO.getCity());
        property.setNeighborhood(propertyDTO.getNeighborhood());
        property.setAddress(propertyDTO.getAddress());
        property.setLatitude(propertyDTO.getLatitude());
        property.setLongitude(propertyDTO.getLongitude());
        property.setIsPublished(propertyDTO.getIsPublished());
        property.setFeatures(propertyDTO.getFeatures());

        return propertyRepository.save(property);
    }

    public Property updatePropertyStatus(String id, Property.PropertyStatus status, String ownerId) {
        Property property = getPropertyById(id);

        if (!property.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Vous n'avez pas le droit de modifier cette propriété");
        }

        property.setStatus(status);
        log.info("Statut de la propriété {} changé à {}", id, status);
        return propertyRepository.save(property);
    }

    public Property assignAgent(String propertyId, String agentId, String ownerId) {
        Property property = getPropertyById(propertyId);

        if (!property.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Vous n'avez pas le droit de modifier cette propriété");
        }

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent non trouvé"));

        property.setAgentId(agent.getId());
        property.setAgentName(agent.getFullName());
        log.info("Agent {} assigné à la propriété {}", agentId, propertyId);
        return propertyRepository.save(property);
    }

    public Property publishProperty(String id, Boolean publish, String ownerId) {
        Property property = getPropertyById(id);

        if (!property.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Vous n'avez pas le droit de modifier cette propriété");
        }

        property.setIsPublished(publish);
        log.info("Propriété {} publiée: {}", id, publish);
        return propertyRepository.save(property);
    }

    public void deleteProperty(String id, String ownerId) {
        Property property = getPropertyById(id);

        if (!property.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Vous n'avez pas le droit de supprimer cette propriété");
        }

        log.info("Suppression de la propriété: {}", id);
        propertyRepository.delete(property);
    }

    public long countPropertiesByOwner(String ownerId) {
        return propertyRepository.countByOwnerId(ownerId);
    }

    public long countPropertiesByAgent(String agentId) {
        return propertyRepository.countByAgentId(agentId);
    }

    public PropertyDTO convertToDTO(Property property) {
        return PropertyDTO.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .price(property.getPrice())
                .area(property.getArea())
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .propertyType(property.getPropertyType())
                .city(property.getCity())
                .neighborhood(property.getNeighborhood())
                .address(property.getAddress())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .status(property.getStatus())
                .ownerId(property.getOwnerId())
                .ownerName(property.getOwnerName())
                .agentId(property.getAgentId())
                .agentName(property.getAgentName())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .isPublished(property.getIsPublished())
                .features(property.getFeatures())
                .build();
    }
}
