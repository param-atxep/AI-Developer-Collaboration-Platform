package com.foodrescue.geo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodrescue.geo.dto.GeoMatchEvent;
import com.foodrescue.geo.dto.GeoSearchResult;
import com.foodrescue.geo.entity.GeoLocation;
import com.foodrescue.geo.service.GeoLocationService;
import com.foodrescue.geo.dto.GeoSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private final ObjectMapper objectMapper;

    @Bean
    public ConsumerFactory<String, Map<String, Object>> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Configuration
    @RequiredArgsConstructor
    @Slf4j
    static class FoodListedEventListener {

        private final GeoLocationService geoLocationService;
        private final KafkaTemplate<String, Object> kafkaTemplate;

        private static final String GEO_MATCHED_TOPIC = "notification.geo-matched";
        private static final double DEFAULT_SEARCH_RADIUS_KM = 15.0;

        @SuppressWarnings("unchecked")
        @KafkaListener(topics = "food.listed", groupId = "${spring.kafka.consumer.group-id}")
        public void handleFoodListedEvent(Map<String, Object> event) {
            log.info("Received food.listed event: {}", event);

            try {
                double latitude = ((Number) event.get("latitude")).doubleValue();
                double longitude = ((Number) event.get("longitude")).doubleValue();
                UUID foodListingId = UUID.fromString((String) event.get("foodListingId"));

                // Search for nearby NGOs and citizens
                GeoSearchRequest ngoRequest = GeoSearchRequest.builder()
                        .latitude(latitude)
                        .longitude(longitude)
                        .radiusKm(DEFAULT_SEARCH_RADIUS_KM)
                        .entityType(GeoLocation.EntityType.NGO)
                        .page(0)
                        .size(50)
                        .build();

                GeoSearchRequest citizenRequest = GeoSearchRequest.builder()
                        .latitude(latitude)
                        .longitude(longitude)
                        .radiusKm(DEFAULT_SEARCH_RADIUS_KM)
                        .entityType(GeoLocation.EntityType.CITIZEN)
                        .page(0)
                        .size(50)
                        .build();

                List<GeoSearchResult> nearbyNGOs = geoLocationService.findNearby(ngoRequest);
                List<GeoSearchResult> nearbyCitizens = geoLocationService.findNearby(citizenRequest);

                log.info("Found {} nearby NGOs and {} nearby citizens for food listing {}",
                        nearbyNGOs.size(), nearbyCitizens.size(), foodListingId);

                // Produce geo-matched events for each nearby entity
                for (GeoSearchResult ngo : nearbyNGOs) {
                    GeoMatchEvent matchEvent = buildMatchEvent(foodListingId, ngo);
                    kafkaTemplate.send(GEO_MATCHED_TOPIC, foodListingId.toString(), matchEvent);
                }

                for (GeoSearchResult citizen : nearbyCitizens) {
                    GeoMatchEvent matchEvent = buildMatchEvent(foodListingId, citizen);
                    kafkaTemplate.send(GEO_MATCHED_TOPIC, foodListingId.toString(), matchEvent);
                }

                log.info("Published {} geo-matched events for food listing {}",
                        nearbyNGOs.size() + nearbyCitizens.size(), foodListingId);

            } catch (Exception e) {
                log.error("Error processing food.listed event: {}", e.getMessage(), e);
            }
        }

        private GeoMatchEvent buildMatchEvent(UUID foodListingId, GeoSearchResult result) {
            return GeoMatchEvent.builder()
                    .foodListingId(foodListingId)
                    .matchedEntityId(result.getEntityId())
                    .matchedEntityType(result.getEntityType())
                    .distanceKm(result.getDistanceKm())
                    .latitude(result.getLatitude())
                    .longitude(result.getLongitude())
                    .address(result.getAddress())
                    .city(result.getCity())
                    .matchedAt(LocalDateTime.now())
                    .build();
        }
    }
}
