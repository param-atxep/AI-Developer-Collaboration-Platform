-- ============================================================================
-- V4: Pickups — Claiming and collecting food listings
-- AI Food Waste Redistribution Platform
-- ============================================================================

-- ============================================================================
-- Pickups table
-- ============================================================================
CREATE TABLE pickups (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    food_listing_id UUID NOT NULL REFERENCES food_listings(id) ON DELETE CASCADE,
    restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    claimer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    claimer_type VARCHAR(20) NOT NULL CHECK (claimer_type IN (
        'NGO', 'CITIZEN', 'VOLUNTEER'
    )),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN (
        'SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW'
    )),
    scheduled_pickup_time TIMESTAMP NOT NULL,
    actual_pickup_time TIMESTAMP,
    notes TEXT,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    feedback TEXT,
    qr_code VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- Indexes
-- ============================================================================
CREATE INDEX idx_pickups_food_listing_id ON pickups(food_listing_id);
CREATE INDEX idx_pickups_claimer_id ON pickups(claimer_id);
CREATE INDEX idx_pickups_restaurant_id ON pickups(restaurant_id);
CREATE INDEX idx_pickups_status ON pickups(status);
CREATE INDEX idx_pickups_scheduled_time ON pickups(scheduled_pickup_time);
CREATE INDEX idx_pickups_claimer_type ON pickups(claimer_type);

-- Composite index for active pickups per claimer
CREATE INDEX idx_pickups_claimer_active ON pickups(claimer_id, status)
    WHERE status IN ('SCHEDULED', 'IN_PROGRESS');

-- ============================================================================
-- Trigger: auto-update updated_at on pickups
-- ============================================================================
CREATE OR REPLACE FUNCTION update_pickups_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_pickups_updated_at
    BEFORE UPDATE ON pickups
    FOR EACH ROW
    EXECUTE FUNCTION update_pickups_updated_at();
