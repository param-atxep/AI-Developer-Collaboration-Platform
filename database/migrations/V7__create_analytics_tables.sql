-- ============================================================================
-- V7: Analytics — Metrics, platform stats, and restaurant analytics
-- AI Food Waste Redistribution Platform
-- ============================================================================

-- ============================================================================
-- Daily food-saved metrics per restaurant
-- ============================================================================
CREATE TABLE food_saved_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    total_listings INTEGER DEFAULT 0,
    total_claimed INTEGER DEFAULT 0,
    total_picked_up INTEGER DEFAULT 0,
    total_expired INTEGER DEFAULT 0,
    food_saved_kg DOUBLE PRECISION DEFAULT 0,
    co2_saved_kg DOUBLE PRECISION DEFAULT 0,
    meals_provided INTEGER DEFAULT 0,
    monetary_value_saved DECIMAL(10, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- Platform-wide daily metrics (one row per day)
-- ============================================================================
CREATE TABLE platform_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    date DATE NOT NULL UNIQUE,
    total_users INTEGER DEFAULT 0,
    active_users INTEGER DEFAULT 0,
    total_restaurants INTEGER DEFAULT 0,
    total_ngos INTEGER DEFAULT 0,
    total_listings INTEGER DEFAULT 0,
    total_pickups INTEGER DEFAULT 0,
    total_food_saved_kg DOUBLE PRECISION DEFAULT 0,
    total_meals_provided INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- Restaurant analytics — aggregated by configurable periods
-- ============================================================================
CREATE TABLE restaurant_analytics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    period VARCHAR(20) NOT NULL CHECK (period IN (
        'DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY'
    )),
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    waste_generated DOUBLE PRECISION DEFAULT 0,
    waste_redirected DOUBLE PRECISION DEFAULT 0,
    waste_reduction_percent DOUBLE PRECISION DEFAULT 0,
    top_categories JSONB,
    peak_waste_hours JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- Indexes
-- ============================================================================
-- food_saved_metrics
CREATE INDEX idx_food_saved_metrics_restaurant_id ON food_saved_metrics(restaurant_id);
CREATE INDEX idx_food_saved_metrics_date ON food_saved_metrics(date);
CREATE UNIQUE INDEX idx_food_saved_metrics_restaurant_date ON food_saved_metrics(restaurant_id, date);

-- platform_metrics
CREATE INDEX idx_platform_metrics_date ON platform_metrics(date);

-- restaurant_analytics
CREATE INDEX idx_restaurant_analytics_restaurant_id ON restaurant_analytics(restaurant_id);
CREATE INDEX idx_restaurant_analytics_period ON restaurant_analytics(period);
CREATE INDEX idx_restaurant_analytics_period_start ON restaurant_analytics(period_start);
CREATE UNIQUE INDEX idx_restaurant_analytics_restaurant_period ON restaurant_analytics(restaurant_id, period, period_start);
