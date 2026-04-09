package com.immobilier.service;

import com.immobilier.dto.PropertyDTO;
import com.immobilier.entity.Property;
import com.immobilier.entity.RoleEnum;
import com.immobilier.entity.User;
import com.immobilier.exception.ResourceNotFoundException;
import com.immobilier.repository.PropertyRepository;
import com.immobilier.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private PropertyService propertyService;

    private Property testProperty;
    private User testOwner;
    private PropertyDTO testPropertyDTO;

    @BeforeEach
    public void setUp() {
        testOwner = User.builder()
                .id("owner-id-1")
                .username("owner")
                .email("owner@example.com")
                .fullName("Property Owner")
                .role(RoleEnum.ROLE_SELLER)
                .isActive(true)
                .build();

        testProperty = Property.builder()
                .id("prop-id-1")
                .title("Beautiful House")
                .description("A beautiful house in the city")
                .price(500000.0)
                .area(200.0)
                .bedrooms(4)
                .bathrooms(2)
                .propertyType(Property.PropertyType.HOUSE)
                .city("Bamenda")
                .neighborhood("Downtown")
                .address("123 Main St")
                .ownerId("owner-id-1")
                .ownerName("Property Owner")
                .status(Property.PropertyStatus.AVAILABLE)
                .isPublished(true)
                .build();

        testPropertyDTO = PropertyDTO.builder()
                .id("prop-id-1")
                .title("Beautiful House")
                .description("A beautiful house in the city")
                .price(500000.0)
                .area(200.0)
                .bedrooms(4)
                .bathrooms(2)
                .propertyType(Property.PropertyType.HOUSE)
                .city("Bamenda")
                .neighborhood("Downtown")
                .address("123 Main St")
                .status(Property.PropertyStatus.AVAILABLE)
                .isPublished(true)
                .build();
    }

    @Test
    public void testCreateProperty_Success() {
        when(userRepository.findById("owner-id-1")).thenReturn(Optional.of(testOwner));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);

        Property createdProperty = propertyService.createProperty(testPropertyDTO, "owner-id-1");

        assertNotNull(createdProperty);
        assertEquals("Beautiful House", createdProperty.getTitle());
        assertEquals("owner-id-1", createdProperty.getOwnerId());
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    public void testCreateProperty_OwnerNotFound() {
        when(userRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                propertyService.createProperty(testPropertyDTO, "unknown-id"));
    }

    @Test
    public void testGetPropertyById_Success() {
        when(propertyRepository.findById("prop-id-1")).thenReturn(Optional.of(testProperty));

        Property property = propertyService.getPropertyById("prop-id-1");

        assertNotNull(property);
        assertEquals("prop-id-1", property.getId());
        assertEquals("Beautiful House", property.getTitle());
        verify(propertyRepository, times(1)).findById("prop-id-1");
    }

    @Test
    public void testGetPropertyById_NotFound() {
        when(propertyRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                propertyService.getPropertyById("unknown-id"));
    }

    @Test
    public void testGetAllPublishedProperties_Success() {
        List<Property> properties = List.of(testProperty);
        Page<Property> pageProperties = new PageImpl<>(properties);
        Pageable pageable = PageRequest.of(0, 10);

        when(propertyRepository.findAllPublishedAndAvailable(pageable)).thenReturn(pageProperties);

        Page<Property> result = propertyService.getAllPublishedProperties(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(propertyRepository, times(1)).findAllPublishedAndAvailable(pageable);
    }

    @Test
    public void testGetPropertiesByOwner_Success() {
        List<Property> properties = List.of(testProperty);
        when(propertyRepository.findByOwnerId("owner-id-1")).thenReturn(properties);

        List<Property> result = propertyService.getPropertiesByOwner("owner-id-1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Beautiful House", result.get(0).getTitle());
        verify(propertyRepository, times(1)).findByOwnerId("owner-id-1");
    }

    @Test
    public void testUpdateProperty_Success() {
        when(propertyRepository.findById("prop-id-1")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);

        Property updatedProperty = propertyService.updateProperty("prop-id-1", testPropertyDTO, "owner-id-1");

        assertNotNull(updatedProperty);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    public void testUpdateProperty_UnauthorizedOwner() {
        when(propertyRepository.findById("prop-id-1")).thenReturn(Optional.of(testProperty));

        assertThrows(IllegalArgumentException.class, () ->
                propertyService.updateProperty("prop-id-1", testPropertyDTO, "other-owner-id"));
    }

    @Test
    public void testUpdatePropertyStatus_Success() {
        when(propertyRepository.findById("prop-id-1")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);

        Property updatedProperty = propertyService.updatePropertyStatus("prop-id-1", Property.PropertyStatus.SOLD, "owner-id-1");

        assertNotNull(updatedProperty);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    public void testAssignAgent_Success() {
        User agent = User.builder()
                .id("agent-id-1")
                .username("agent")
                .email("agent@example.com")
                .fullName("Real Estate Agent")
                .role(RoleEnum.ROLE_AGENT)
                .isActive(true)
                .build();

        when(propertyRepository.findById("prop-id-1")).thenReturn(Optional.of(testProperty));
        when(userRepository.findById("agent-id-1")).thenReturn(Optional.of(agent));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);

        Property updatedProperty = propertyService.assignAgent("prop-id-1", "agent-id-1", "owner-id-1");

        assertNotNull(updatedProperty);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    public void testPublishProperty_Success() {
        when(propertyRepository.findById("prop-id-1")).thenReturn(Optional.of(testProperty));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);

        Property updatedProperty = propertyService.publishProperty("prop-id-1", false, "owner-id-1");

        assertNotNull(updatedProperty);
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    public void testDeleteProperty_Success() {
        when(propertyRepository.findById("prop-id-1")).thenReturn(Optional.of(testProperty));
        doNothing().when(propertyRepository).delete(any(Property.class));

        propertyService.deleteProperty("prop-id-1", "owner-id-1");

        verify(propertyRepository, times(1)).delete(any(Property.class));
    }

    @Test
    public void testDeleteProperty_UnauthorizedOwner() {
        when(propertyRepository.findById("prop-id-1")).thenReturn(Optional.of(testProperty));

        assertThrows(IllegalArgumentException.class, () ->
                propertyService.deleteProperty("prop-id-1", "other-owner-id"));
    }

    @Test
    public void testSearchProperties_Success() {
        List<Property> properties = List.of(testProperty);
        Pageable pageable = PageRequest.of(0, 10);

        when(mongoTemplate.count(any(Query.class), eq(Property.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Property.class))).thenReturn(properties);

        Page<Property> result = propertyService.searchProperties("Bamenda", 400000.0, 600000.0, 4, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Beautiful House", result.getContent().get(0).getTitle());
    }

    @Test
    public void testConvertToDTO() {
        PropertyDTO dto = propertyService.convertToDTO(testProperty);

        assertNotNull(dto);
        assertEquals(testProperty.getId(), dto.getId());
        assertEquals(testProperty.getTitle(), dto.getTitle());
        assertEquals(testProperty.getPrice(), dto.getPrice());
        assertEquals(testProperty.getOwnerId(), dto.getOwnerId());
    }
}
