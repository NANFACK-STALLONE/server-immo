package com.immobilier.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property implements Serializable {

    @Id
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
    private PropertyType propertyType;

    @NotBlank(message = "La ville est requise")
    private String city;

    @NotBlank(message = "Le quartier est requis")
    private String neighborhood;

    private String address;
    private Double latitude;
    private Double longitude;

    @Builder.Default
    private PropertyStatus status = PropertyStatus.AVAILABLE;

    private String ownerId;
    private String ownerName;
    private String agentId;
    private String agentName;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder.Default
    private Boolean isPublished = true;

    @Builder.Default
    private List<String> features = new ArrayList<>();

    public enum PropertyType {
        APARTMENT("Appartement"),
        HOUSE("Maison"),
        LAND("Terrain"),
        COMMERCIAL("Commercial"),
        OFFICE("Bureau"),
        VILLA("Villa");

        private final String label;

        PropertyType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum PropertyStatus {
        AVAILABLE("Disponible"),
        RESERVED("Réservée"),
        SOLD("Vendue"),
        RENT("Louer");

        private final String label;

        PropertyStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
