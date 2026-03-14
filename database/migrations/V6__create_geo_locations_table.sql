-- ============================================================================
-- V6: Geo Locations — Centralized spatial data for all entities
-- AI Food Waste Redistribution Platform
-- ============================================================================

-- ============================================================================
-- Geo locations table
-- ============================================================================
CREATE TABLE geo_locations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_id UUID NOT NULL,
    entity_type VARCHAR(30) NOT NULL CHECK (entity_type IN (
        'RESTAURANT', 'NGO', 'CITIZEN', 'VOLUNTEER', 'FOOD_LISTING', 'PICKUP'
    )),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100) DEFAULT 'US',
    postal_code VARCHAR(20),
    geom GEOMETRY(Point, 4326),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- Indexes
-- ============================================================================
CREATE INDEX idx_geo_locations_entity_id ON geo_locations(entity_id);
CREATE INDEX idx_geo_locations_entity_type ON geo_locations(entity_type);
CREATE INDEX idx_geo_locations_lat_lng ON geo_locations(latitude, longitude);
CREATE INDEX idx_geo_locations_city ON geo_locations(city);
CREATE INDEX idx_geo_locations_postal_code ON geo_locations(postal_code);

-- Composite index for entity lookups
CREATE UNIQUE INDEX idx_geo_locations_entity ON geo_locations(entity_id, entity_type);

-- PostGIS spatial index for proximity queries
CREATE INDEX idx_geo_locations_geom ON geo_locations USING GIST(geom);

-- ============================================================================
-- Trigger: auto-populate the PostGIS geometry column from lat/lng
-- ============================================================================
CREATE OR REPLACE FUNCTION update_geo_locations_geom()
RETURNS TRIGGER AS $$
BEGIN
    NEW.geom = ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326);
    NEW.last_updated = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_geo_locations_geom
    BEFORE INSERT OR UPDATE ON geo_locations
    FOR EACH ROW
    EXECUTE FUNCTION update_geo_locations_geom();
