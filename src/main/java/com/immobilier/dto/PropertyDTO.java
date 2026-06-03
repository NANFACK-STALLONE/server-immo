package com.immobilier.dto;

import com.immobilier.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDTO {

    private String id;

    @NotBlank(message = "Le titre est requis")
    private String title;

    @NotBlank(message = "La description est requise")
    private String description;

    @NotNull(message = "Le prix est requis")
    @Positive(message = "Le prix doit être positif")
    private Double price;

    @NotNull(message = "La surface est requise")
    @Positive(message = "La surface doit être positive")
    private Double area;

    @NotNull(message = "Le nombre de chambres est requis")
    @Positive(message = "Le nombre de chambres doit être positif")
    private Integer bedrooms;

    @NotNull(message = "Le nombre de salles de bain est requis")
    @Positive(message = "Le nombre de salles de bain doit être positif")
    private Integer bathrooms;

    @NotNull(message = "Le type est requis")
    private Property.PropertyType propertyType;

    @NotBlank(message = "La ville est requise")
    private String city;

    @NotBlank(message = "Le quartier est requis")
    private String neighborhood;

    private String address;
    private Double latitude;
    private Double longitude;
    private Property.PropertyStatus status;
    private String ownerId;
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;
    private String agentId;
    private String agentName;
    private String agentPhone;
    private String agentEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPublished;
    private List<String> features;
}
