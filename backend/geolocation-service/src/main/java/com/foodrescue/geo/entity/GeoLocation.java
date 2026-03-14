package com.foodrescue.geo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "geo_locations", indexes = {
        @Index(name = "idx_entity_id", columnList = "entityId"),
        @Index(name = "idx_entity_type", columnList = "entityType"),
        @Index(name = "idx_lat_lng", columnList = "latitude, longitude")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeoLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityType entityType;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 20)
    private String postalCode;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    public enum EntityType {
        RESTAURANT,
        NGO,
        CITIZEN
    }
}
