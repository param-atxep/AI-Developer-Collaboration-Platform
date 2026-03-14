-- ============================================================================
-- V2: User Profiles, Restaurant Profiles, and NGO Profiles
-- AI Food Waste Redistribution Platform
-- ============================================================================

-- ============================================================================
-- User profiles — shared profile data for all user types
-- ============================================================================
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    organization_name VARCHAR(255),
    description TEXT,
    address TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    profile_image_url VARCHAR(500),
    rating DOUBLE PRECISION DEFAULT 0,
    total_donations INTEGER DEFAULT 0,
    total_pickups INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- Restaurant-specific profile details
-- ============================================================================
CREATE TABLE restaurants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_profile_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    cuisine_type VARCHAR(100),
    operating_hours JSONB,
    food_safety_rating VARCHAR(10),
    license_number VARCHAR(100),
    average_waste_per_day DOUBLE PRECISION
);

-- ============================================================================
-- NGO-specific profile details
-- ============================================================================
CREATE TABLE ngo_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_profile_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    registration_number VARCHAR(100),
    service_area TEXT,
    capacity INTEGER,
    people_served INTEGER
);

-- ============================================================================
-- Indexes
-- ============================================================================
CREATE UNIQUE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_lat_lng ON user_profiles(latitude, longitude);
CREATE INDEX idx_user_profiles_is_active ON user_profiles(is_active);
CREATE INDEX idx_restaurants_user_profile_id ON restaurants(user_profile_id);
CREATE INDEX idx_ngo_profiles_user_profile_id ON ngo_profiles(user_profile_id);

-- ============================================================================
-- Trigger: auto-update updated_at on user_profiles
-- ============================================================================
CREATE OR REPLACE FUNCTION update_user_profiles_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_user_profiles_updated_at
    BEFORE UPDATE ON user_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_user_profiles_updated_at();
