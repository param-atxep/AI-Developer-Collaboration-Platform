-- ============================================================================
-- V3: Food Listings
-- AI Food Waste Redistribution Platform
-- ============================================================================

-- ============================================================================
-- Food listings — surplus food posted by restaurants
-- ============================================================================
CREATE TABLE food_listings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    food_category VARCHAR(50) NOT NULL CHECK (food_category IN (
        'PREPARED_MEALS', 'RAW_INGREDIENTS', 'BAKED_GOODS',
        'DAIRY', 'FRUITS_VEGETABLES', 'BEVERAGES',
        'CANNED_GOODS', 'FROZEN_FOOD', 'OTHER'
    )),
    quantity DOUBLE PRECISION NOT NULL,
    unit VARCHAR(20) NOT NULL CHECK (unit IN (
        'KG', 'LBS', 'LITERS', 'SERVINGS', 'ITEMS', 'BOXES', 'TRAYS'
    )),
    original_price DECIMAL(10, 2),
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN (
        'AVAILABLE', 'CLAIMED', 'PICKED_UP', 'EXPIRED', 'CANCELLED'
    )),
    image_url VARCHAR(500),
    allergens TEXT,
    is_vegetarian BOOLEAN DEFAULT FALSE,
    is_vegan BOOLEAN DEFAULT FALSE,
    is_halal BOOLEAN DEFAULT FALSE,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- Indexes
-- ============================================================================
CREATE INDEX idx_food_listings_status ON food_listings(status);
CREATE INDEX idx_food_listings_restaurant_id ON food_listings(restaurant_id);
CREATE INDEX idx_food_listings_expires_at ON food_listings(expires_at);
CREATE INDEX idx_food_listings_food_category ON food_listings(food_category);
CREATE INDEX idx_food_listings_lat_lng ON food_listings(latitude, longitude);
CREATE INDEX idx_food_listings_created_at ON food_listings(created_at DESC);

-- Composite index for common query: available listings not yet expired
CREATE INDEX idx_food_listings_available ON food_listings(status, expires_at)
    WHERE status = 'AVAILABLE';

-- ============================================================================
-- Trigger: auto-update updated_at on food_listings
-- ============================================================================
CREATE OR REPLACE FUNCTION update_food_listings_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_food_listings_updated_at
    BEFORE UPDATE ON food_listings
    FOR EACH ROW
    EXECUTE FUNCTION update_food_listings_updated_at();
