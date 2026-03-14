-- ============================================================================
-- Seed Data for AI Food Waste Redistribution Platform
-- Realistic sample data for development and testing
-- ============================================================================

-- ============================================================================
-- 1. ADMIN USER
-- ============================================================================
-- Password: Admin@123 (bcrypt hashed)
INSERT INTO users (id, email, password, first_name, last_name, role, phone, verified)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'admin@foodwaste.org',
    '$2a$12$LJ3m4ys3Rl40lRkGvMBseeXNhDaQLSBFCuge.DJFX8vGGCwbmBqHO',
    'System',
    'Administrator',
    'ADMIN',
    '+1-555-000-0001',
    TRUE
);

INSERT INTO user_profiles (id, user_id, organization_name, description, address, latitude, longitude, is_active)
VALUES (
    'b0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000001',
    'FoodWaste Platform',
    'Platform administration',
    '100 Main Street, San Francisco, CA 94105',
    37.7749,
    -122.4194,
    TRUE
);

-- ============================================================================
-- 2. RESTAURANT USERS (5 restaurants)
-- ============================================================================
-- Password for all sample users: Test@1234 (bcrypt hashed)

-- Restaurant 1: The Green Kitchen
INSERT INTO users (id, email, password, first_name, last_name, role, phone, verified)
VALUES (
    'a0000000-0000-0000-0000-000000000010',
    'contact@greenkitchen.com',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Maria',
    'Gonzalez',
    'RESTAURANT',
    '+1-555-100-0001',
    TRUE
);

INSERT INTO user_profiles (id, user_id, organization_name, description, address, latitude, longitude, rating, total_donations, is_active)
VALUES (
    'b0000000-0000-0000-0000-000000000010',
    'a0000000-0000-0000-0000-000000000010',
    'The Green Kitchen',
    'Farm-to-table restaurant committed to zero waste. We donate all surplus food daily.',
    '456 Market Street, San Francisco, CA 94105',
    37.7897,
    -122.3999,
    4.8,
    156,
    TRUE
);

INSERT INTO restaurants (id, user_profile_id, cuisine_type, operating_hours, food_safety_rating, license_number, average_waste_per_day)
VALUES (
    'c0000000-0000-0000-0000-000000000010',
    'b0000000-0000-0000-0000-000000000010',
    'American',
    '{"monday": {"open": "07:00", "close": "22:00"}, "tuesday": {"open": "07:00", "close": "22:00"}, "wednesday": {"open": "07:00", "close": "22:00"}, "thursday": {"open": "07:00", "close": "23:00"}, "friday": {"open": "07:00", "close": "23:00"}, "saturday": {"open": "08:00", "close": "23:00"}, "sunday": {"open": "08:00", "close": "21:00"}}',
    'A',
    'SF-REST-2024-1001',
    12.5
);

-- Restaurant 2: Sakura Sushi Bar
INSERT INTO users (id, email, password, first_name, last_name, role, phone, verified)
VALUES (
    'a0000000-0000-0000-0000-000000000011',
    'info@sakurasushi.com',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Kenji',
    'Tanaka',
    'RESTAURANT',
    '+1-555-100-0002',
    TRUE
);

INSERT INTO user_profiles (id, user_id, organization_name, description, address, latitude, longitude, rating, total_donations, is_active)
VALUES (
    'b0000000-0000-0000-0000-000000000011',
    'a0000000-0000-0000-0000-000000000011',
    'Sakura Sushi Bar',
    'Authentic Japanese cuisine with daily fresh fish. Surplus sushi donated at close.',
    '789 Geary Street, San Francisco, CA 94109',
    37.7862,
    -122.4140,
    4.6,
    98,
    TRUE
);

INSERT INTO restaurants (id, user_profile_id, cuisine_type, operating_hours, food_safety_rating, license_number, average_waste_per_day)
VALUES (
    'c0000000-0000-0000-0000-000000000011',
    'b0000000-0000-0000-0000-000000000011',
    'Japanese',
    '{"monday": {"open": "11:00", "close": "22:00"}, "tuesday": {"open": "11:00", "close": "22:00"}, "wednesday": {"open": "11:00", "close": "22:00"}, "thursday": {"open": "11:00", "close": "22:00"}, "friday": {"open": "11:00", "close": "23:00"}, "saturday": {"open": "11:00", "close": "23:00"}, "sunday": {"open": "12:00", "close": "21:00"}}',
    'A',
    'SF-REST-2024-1002',
    8.3
);

-- Restaurant 3: Bella Napoli Pizzeria
INSERT INTO users (id, email, password, first_name, last_name, role, phone, verified)
VALUES (
    'a0000000-0000-0000-0000-000000000012',
    'hello@bellanapoli.com',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Giovanni',
    'Rossi',
    'RESTAURANT',
    '+1-555-100-0003',
    TRUE
);

INSERT INTO user_profiles (id, user_id, organization_name, description, address, latitude, longitude, rating, total_donations, is_active)
VALUES (
    'b0000000-0000-0000-0000-000000000012',
    'a0000000-0000-0000-0000-000000000012',
    'Bella Napoli Pizzeria',
    'Wood-fired Neapolitan pizza. Leftover dough and toppings donated to local shelters.',
    '321 Columbus Avenue, San Francisco, CA 94133',
    37.7985,
    -122.4078,
    4.5,
    210,
    TRUE
);

INSERT INTO restaurants (id, user_profile_id, cuisine_type, operating_hours, food_safety_rating, license_number, average_waste_per_day)
VALUES (
    'c0000000-0000-0000-0000-000000000012',
    'b0000000-0000-0000-0000-000000000012',
    'Italian',
    '{"monday": {"open": "11:00", "close": "22:00"}, "tuesday": {"open": "11:00", "close": "22:00"}, "wednesday": {"open": "11:00", "close": "22:00"}, "thursday": {"open": "11:00", "close": "22:00"}, "friday": {"open": "11:00", "close": "23:30"}, "saturday": {"open": "11:00", "close": "23:30"}, "sunday": {"open": "12:00", "close": "21:00"}}',
    'A',
    'SF-REST-2024-1003',
    15.0
);

-- Restaurant 4: Mumbai Masala
INSERT INTO users (id, email, password, first_name, last_name, role, phone, verified)
VALUES (
    'a0000000-0000-0000-0000-000000000013',
    'orders@mumbaimasala.com',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Priya',
    'Sharma',
    'RESTAURANT',
    '+1-555-100-0004',
    TRUE
);

INSERT INTO user_profiles (id, user_id, organization_name, description, address, latitude, longitude, rating, total_donations, is_active)
VALUES (
    'b0000000-0000-0000-0000-000000000013',
    'a0000000-0000-0000-0000-000000000013',
    'Mumbai Masala',
    'Authentic Indian cuisine. Our buffet surplus goes to families in need every evening.',
    '555 Valencia Street, San Francisco, CA 94110',
    37.7633,
    -122.4215,
    4.7,
    180,
    TRUE
);

INSERT INTO restaurants (id, user_profile_id, cuisine_type, operating_hours, food_safety_rating, license_number, average_waste_per_day)
VALUES (
    'c0000000-0000-0000-0000-000000000013',
    'b0000000-0000-0000-0000-000000000013',
    'Indian',
    '{"monday": {"open": "11:30", "close": "22:00"}, "tuesday": {"open": "11:30", "close": "22:00"}, "wednesday": {"open": "11:30", "close": "22:00"}, "thursday": {"open": "11:30", "close": "22:00"}, "friday": {"open": "11:30", "close": "23:00"}, "saturday": {"open": "11:00", "close": "23:00"}, "sunday": {"open": "11:00", "close": "21:30"}}',
    'B',
    'SF-REST-2024-1004',
    18.0
);

-- Restaurant 5: Le Petit Bistro
INSERT INTO users (id, email, password, first_name, last_name, role, phone, verified)
VALUES (
    'a0000000-0000-0000-0000-000000000014',
    'reservations@lepetitbistro.com',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Claire',
    'Dubois',
    'RESTAURANT',
    '+1-555-100-0005',
    TRUE
);

INSERT INTO user_profiles (id, user_id, organization_name, description, address, latitude, longitude, rating, total_donations, is_active)
VALUES (
    'b0000000-0000-0000-0000-000000000014',
    'a0000000-0000-0000-0000-000000000014',
    'Le Petit Bistro',
    'Classic French cuisine. Daily pastries and prepared meals donated before closing.',
    '888 Union Street, San Francisco, CA 94123',
    37.7980,
    -122.4280,
    4.9,
    245,
    TRUE
);

INSERT INTO restaurants (id, user_profile_id, cuisine_type, operating_hours, food_safety_rating, license_number, average_waste_per_day)
VALUES (
    'c0000000-0000-0000-0000-000000000014',
    'b0000000-0000-0000-0000-000000000014',
    'French',
    '{"monday": {"open": "closed", "close": "closed"}, "tuesday": {"open": "17:00", "close": "22:00"}, "wednesday": {"open": "17:00", "close": "22:00"}, "thursday": {"open": "17:00", "close": "22:00"}, "friday": {"open": "17:00", "close": "23:00"}, "saturday": {"open": "10:00", "close": "23:00"}, "sunday": {"open": "10:00", "close": "21:00"}}',
    'A',
    'SF-REST-2024-1005',
    10.5
);

-- ============================================================================
-- 3. NGO USERS (3 NGOs)
-- ============================================================================

-- NGO 1: SF Food Bank
INSERT INTO users (id, email, password, first_name, last_name, role, phone, verified)
VALUES (
    'a0000000-0000-0000-0000-000000000020',
    'operations@sffoodbank.org',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'James',
    'Wilson',
    'NGO',
    '+1-555-200-0001',
    TRUE
);

INSERT INTO user_profiles (id, user_id, organization_name, description, address, latitude, longitude, rating, total_pickups, is_active)
VALUES (
    'b0000000-0000-0000-0000-000000000020',
    'a0000000-0000-0000-0000-000000000020',
    'SF Community Food Bank',
    'Serving the San Francisco community since 1987. We collect and distribute surplus food to 50+ shelters.',
    '900 Pennsylvania Avenue, San Francisco, CA 94107',
    37.7530,
    -122.3937,
    4.9,
    520,
    TRUE
);

INSERT INTO ngo_profiles (id, user_profile_id, registration_number, service_area, capacity, people_served)
VALUES (
    'd0000000-0000-0000-0000-000000000020',
    'b0000000-0000-0000-0000-000000000020',
    'NGO-CA-2024-5001',
    'San Francisco, South San Francisco, Daly City',
    5000,
    3200
);

-- NGO 2: Meals on Wheels SF
INSERT INTO users (id, email, password, first_name, last_name, role, phone, verified)
VALUES (
    'a0000000-0000-0000-0000-000000000021',
    'dispatch@mealsonwheelssf.org',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Sarah',
    'Chen',
    'NGO',
    '+1-555-200-0002',
    TRUE
);

INSERT INTO user_profiles (id, user_id, organization_name, description, address, latitude, longitude, rating, total_pickups, is_active)
VALUES (
    'b0000000-0000-0000-0000-000000000021',
    'a0000000-0000-0000-0000-000000000021',
    'Meals on Wheels SF',
    'Delivering nutritious meals to homebound seniors and disabled individuals.',
    '1375 Fairfax Avenue, San Francisco, CA 94124',
    37.7290,
    -122.3872,
    4.7,
    340,
    TRUE
);

INSERT INTO ngo_profiles (id, user_profile_id, registration_number, service_area, capacity, people_served)
VALUES (
    'd0000000-0000-0000-0000-000000000021',
    'b0000000-0000-0000-0000-000000000021',
    'NGO-CA-2024-5002',
    'San Francisco, Oakland',
    2000,
    1800
);

-- NGO 3: Bay Area Hunger Relief
INSERT INTO users (id, email, password, first_name, last_name, role, phone, verified)
VALUES (
    'a0000000-0000-0000-0000-000000000022',
    'team@bayareahungerrelief.org',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Michael',
    'Okafor',
    'NGO',
    '+1-555-200-0003',
    TRUE
);

INSERT INTO user_profiles (id, user_id, organization_name, description, address, latitude, longitude, rating, total_pickups, is_active)
VALUES (
    'b0000000-0000-0000-0000-000000000022',
    'a0000000-0000-0000-0000-000000000022',
    'Bay Area Hunger Relief',
    'Connecting surplus food with families facing food insecurity across the Bay Area.',
    '250 Ellis Street, San Francisco, CA 94102',
    37.7855,
    -122.4110,
    4.8,
    410,
    TRUE
);

INSERT INTO ngo_profiles (id, user_profile_id, registration_number, service_area, capacity, people_served)
VALUES (
    'd0000000-0000-0000-0000-000000000022',
    'b0000000-0000-0000-0000-000000000022',
    'NGO-CA-2024-5003',
    'San Francisco, Berkeley, Richmond, San Mateo',
    8000,
    6500
);

-- ============================================================================
-- 4. CITIZEN USERS (5 citizens)
-- ============================================================================

INSERT INTO users (id, email, password, first_name, last_name, role, phone, verified)
VALUES
    ('a0000000-0000-0000-0000-000000000030', 'alice.johnson@email.com', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Alice', 'Johnson', 'CITIZEN', '+1-555-300-0001', TRUE),
    ('a0000000-0000-0000-0000-000000000031', 'bob.martinez@email.com', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Bob', 'Martinez', 'CITIZEN', '+1-555-300-0002', TRUE),
    ('a0000000-0000-0000-0000-000000000032', 'carol.williams@email.com', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Carol', 'Williams', 'CITIZEN', '+1-555-300-0003', TRUE),
    ('a0000000-0000-0000-0000-000000000033', 'david.park@email.com', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'David', 'Park', 'CITIZEN', '+1-555-300-0004', FALSE),
    ('a0000000-0000-0000-0000-000000000034', 'emma.davis@email.com', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Emma', 'Davis', 'CITIZEN', '+1-555-300-0005', TRUE);

INSERT INTO user_profiles (id, user_id, organization_name, description, address, latitude, longitude, rating, total_pickups, is_active)
VALUES
    ('b0000000-0000-0000-0000-000000000030', 'a0000000-0000-0000-0000-000000000030', NULL, 'Environmentally conscious food enthusiast', '150 Folsom Street, San Francisco, CA 94105', 37.7898, -122.3923, 4.5, 12, TRUE),
    ('b0000000-0000-0000-0000-000000000031', 'a0000000-0000-0000-0000-000000000031', NULL, 'College student looking to reduce food waste', '2130 Fulton Street, San Francisco, CA 94117', 37.7745, -122.4572, 4.2, 8, TRUE),
    ('b0000000-0000-0000-0000-000000000032', 'a0000000-0000-0000-0000-000000000032', NULL, 'Mom of three, love saving food from landfills', '3801 24th Street, San Francisco, CA 94114', 37.7516, -122.4287, 4.8, 25, TRUE),
    ('b0000000-0000-0000-0000-000000000033', 'a0000000-0000-0000-0000-000000000033', NULL, 'New to the platform', '480 Ellis Street, San Francisco, CA 94102', 37.7849, -122.4138, 0, 0, TRUE),
    ('b0000000-0000-0000-0000-000000000034', 'a0000000-0000-0000-0000-000000000034', NULL, 'Volunteer coordinator and food rescue advocate', '1700 Owens Street, San Francisco, CA 94158', 37.7678, -122.3933, 4.9, 45, TRUE);

-- ============================================================================
-- 5. FOOD LISTINGS (10 listings, mix of statuses)
-- ============================================================================

-- Listing 1: AVAILABLE — The Green Kitchen
INSERT INTO food_listings (id, restaurant_id, title, description, food_category, quantity, unit, original_price, expires_at, status, allergens, is_vegetarian, is_vegan, is_halal, latitude, longitude, address)
VALUES (
    'e0000000-0000-0000-0000-000000000001',
    'c0000000-0000-0000-0000-000000000010',
    'Organic Vegetable Soup (8 Servings)',
    'Freshly made organic vegetable soup with carrots, celery, potatoes, and herbs. Prepared today, needs to be picked up by tonight.',
    'PREPARED_MEALS',
    8.0,
    'SERVINGS',
    64.00,
    CURRENT_TIMESTAMP + INTERVAL '6 hours',
    'AVAILABLE',
    'Celery',
    TRUE,
    TRUE,
    TRUE,
    37.7897,
    -122.3999,
    '456 Market Street, San Francisco, CA 94105'
);

-- Listing 2: AVAILABLE — Sakura Sushi Bar
INSERT INTO food_listings (id, restaurant_id, title, description, food_category, quantity, unit, original_price, expires_at, status, allergens, is_vegetarian, is_vegan, is_halal, latitude, longitude, address)
VALUES (
    'e0000000-0000-0000-0000-000000000002',
    'c0000000-0000-0000-0000-000000000011',
    'Assorted Sushi Platter (20 pieces)',
    'Mix of California rolls, salmon nigiri, and tuna maki. Made fresh this afternoon. Must pick up before closing.',
    'PREPARED_MEALS',
    20.0,
    'ITEMS',
    85.00,
    CURRENT_TIMESTAMP + INTERVAL '4 hours',
    'AVAILABLE',
    'Fish, Soy, Sesame',
    FALSE,
    FALSE,
    FALSE,
    37.7862,
    -122.4140,
    '789 Geary Street, San Francisco, CA 94109'
);

-- Listing 3: AVAILABLE — Bella Napoli Pizzeria
INSERT INTO food_listings (id, restaurant_id, title, description, food_category, quantity, unit, original_price, expires_at, status, allergens, is_vegetarian, is_vegan, is_halal, latitude, longitude, address)
VALUES (
    'e0000000-0000-0000-0000-000000000003',
    'c0000000-0000-0000-0000-000000000012',
    'Margherita & Pepperoni Pizza (6 whole pies)',
    'Six large wood-fired pizzas from tonight''s unsold stock. 3 Margherita, 3 Pepperoni. Still warm!',
    'BAKED_GOODS',
    6.0,
    'ITEMS',
    120.00,
    CURRENT_TIMESTAMP + INTERVAL '3 hours',
    'AVAILABLE',
    'Gluten, Dairy',
    FALSE,
    FALSE,
    FALSE,
    37.7985,
    -122.4078,
    '321 Columbus Avenue, San Francisco, CA 94133'
);

-- Listing 4: CLAIMED — Mumbai Masala
INSERT INTO food_listings (id, restaurant_id, title, description, food_category, quantity, unit, original_price, expires_at, status, allergens, is_vegetarian, is_vegan, is_halal, latitude, longitude, address)
VALUES (
    'e0000000-0000-0000-0000-000000000004',
    'c0000000-0000-0000-0000-000000000013',
    'Buffet Surplus — Curry, Rice & Naan (15 servings)',
    'End-of-day buffet surplus: chicken tikka masala, dal makhani, basmati rice, and garlic naan. Packed in takeaway containers.',
    'PREPARED_MEALS',
    15.0,
    'SERVINGS',
    180.00,
    CURRENT_TIMESTAMP + INTERVAL '5 hours',
    'CLAIMED',
    'Dairy, Nuts, Gluten',
    FALSE,
    FALSE,
    TRUE,
    37.7633,
    -122.4215,
    '555 Valencia Street, San Francisco, CA 94110'
);

-- Listing 5: CLAIMED — Le Petit Bistro
INSERT INTO food_listings (id, restaurant_id, title, description, food_category, quantity, unit, original_price, expires_at, status, allergens, is_vegetarian, is_vegan, is_halal, latitude, longitude, address)
VALUES (
    'e0000000-0000-0000-0000-000000000005',
    'c0000000-0000-0000-0000-000000000014',
    'French Pastry Assortment (2 trays)',
    'Two trays of assorted pastries: croissants, pain au chocolat, eclairs, and fruit tarts. Baked this morning.',
    'BAKED_GOODS',
    2.0,
    'TRAYS',
    95.00,
    CURRENT_TIMESTAMP + INTERVAL '8 hours',
    'CLAIMED',
    'Gluten, Dairy, Eggs',
    TRUE,
    FALSE,
    FALSE,
    37.7980,
    -122.4280,
    '888 Union Street, San Francisco, CA 94123'
);

-- Listing 6: PICKED_UP — The Green Kitchen
INSERT INTO food_listings (id, restaurant_id, title, description, food_category, quantity, unit, original_price, expires_at, status, allergens, is_vegetarian, is_vegan, is_halal, latitude, longitude, address)
VALUES (
    'e0000000-0000-0000-0000-000000000006',
    'c0000000-0000-0000-0000-000000000010',
    'Mixed Salad Bowls (10 containers)',
    'Individually packed garden salads with quinoa, chickpeas, roasted vegetables, and vinaigrette.',
    'PREPARED_MEALS',
    10.0,
    'ITEMS',
    120.00,
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    'PICKED_UP',
    'None',
    TRUE,
    TRUE,
    TRUE,
    37.7897,
    -122.3999,
    '456 Market Street, San Francisco, CA 94105'
);

-- Listing 7: PICKED_UP — Bella Napoli Pizzeria
INSERT INTO food_listings (id, restaurant_id, title, description, food_category, quantity, unit, original_price, expires_at, status, allergens, is_vegetarian, is_vegan, is_halal, latitude, longitude, address)
VALUES (
    'e0000000-0000-0000-0000-000000000007',
    'c0000000-0000-0000-0000-000000000012',
    'Pasta Carbonara & Lasagna (5 kg)',
    'Leftover pasta dishes from lunch service. Carbonara and vegetable lasagna, ready to reheat.',
    'PREPARED_MEALS',
    5.0,
    'KG',
    75.00,
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    'PICKED_UP',
    'Gluten, Dairy, Eggs',
    FALSE,
    FALSE,
    FALSE,
    37.7985,
    -122.4078,
    '321 Columbus Avenue, San Francisco, CA 94133'
);

-- Listing 8: EXPIRED — Sakura Sushi Bar
INSERT INTO food_listings (id, restaurant_id, title, description, food_category, quantity, unit, original_price, expires_at, status, allergens, is_vegetarian, is_vegan, is_halal, latitude, longitude, address)
VALUES (
    'e0000000-0000-0000-0000-000000000008',
    'c0000000-0000-0000-0000-000000000011',
    'Edamame & Miso Soup (12 portions)',
    'Steamed edamame beans and fresh miso soup portions.',
    'PREPARED_MEALS',
    12.0,
    'SERVINGS',
    48.00,
    CURRENT_TIMESTAMP - INTERVAL '24 hours',
    'EXPIRED',
    'Soy',
    TRUE,
    TRUE,
    FALSE,
    37.7862,
    -122.4140,
    '789 Geary Street, San Francisco, CA 94109'
);

-- Listing 9: AVAILABLE — Mumbai Masala
INSERT INTO food_listings (id, restaurant_id, title, description, food_category, quantity, unit, original_price, expires_at, status, allergens, is_vegetarian, is_vegan, is_halal, latitude, longitude, address)
VALUES (
    'e0000000-0000-0000-0000-000000000009',
    'c0000000-0000-0000-0000-000000000013',
    'Fresh Fruit & Vegetable Box (3 boxes)',
    'Mixed boxes of fresh produce: tomatoes, onions, bell peppers, mangoes, and bananas. Perfectly good but cosmetically imperfect.',
    'FRUITS_VEGETABLES',
    3.0,
    'BOXES',
    45.00,
    CURRENT_TIMESTAMP + INTERVAL '24 hours',
    'AVAILABLE',
    'None',
    TRUE,
    TRUE,
    TRUE,
    37.7633,
    -122.4215,
    '555 Valencia Street, San Francisco, CA 94110'
);

-- Listing 10: CANCELLED — Le Petit Bistro
INSERT INTO food_listings (id, restaurant_id, title, description, food_category, quantity, unit, original_price, expires_at, status, allergens, is_vegetarian, is_vegan, is_halal, latitude, longitude, address)
VALUES (
    'e0000000-0000-0000-0000-000000000010',
    'c0000000-0000-0000-0000-000000000014',
    'Cheese Platter & Baguettes',
    'Artisan cheese selection with freshly baked baguettes. Listing cancelled — used for private event instead.',
    'DAIRY',
    2.5,
    'KG',
    60.00,
    CURRENT_TIMESTAMP + INTERVAL '2 hours',
    'CANCELLED',
    'Dairy, Gluten',
    TRUE,
    FALSE,
    FALSE,
    37.7980,
    -122.4280,
    '888 Union Street, San Francisco, CA 94123'
);

-- ============================================================================
-- 6. PICKUPS
-- ============================================================================

-- Pickup 1: COMPLETED — NGO picked up salad bowls
INSERT INTO pickups (id, food_listing_id, restaurant_id, claimer_id, claimer_type, status, scheduled_pickup_time, actual_pickup_time, notes, rating, feedback, qr_code)
VALUES (
    'f0000000-0000-0000-0000-000000000001',
    'e0000000-0000-0000-0000-000000000006',
    'c0000000-0000-0000-0000-000000000010',
    'a0000000-0000-0000-0000-000000000020',
    'NGO',
    'COMPLETED',
    CURRENT_TIMESTAMP - INTERVAL '3 hours',
    CURRENT_TIMESTAMP - INTERVAL '2 hours 45 minutes',
    'Picked up all 10 containers. Will distribute at the evening shelter meal.',
    5,
    'Excellent quality food, always well-packaged. The Green Kitchen is a fantastic partner!',
    'QR-PICKUP-001-GK-SFCFB'
);

-- Pickup 2: COMPLETED — Citizen picked up pasta
INSERT INTO pickups (id, food_listing_id, restaurant_id, claimer_id, claimer_type, status, scheduled_pickup_time, actual_pickup_time, notes, rating, feedback, qr_code)
VALUES (
    'f0000000-0000-0000-0000-000000000002',
    'e0000000-0000-0000-0000-000000000007',
    'c0000000-0000-0000-0000-000000000012',
    'a0000000-0000-0000-0000-000000000032',
    'CITIZEN',
    'COMPLETED',
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '1 hour 50 minutes',
    'Shared with my neighbors. Everyone loved the lasagna!',
    5,
    'Generous portions and delicious food. Thank you Bella Napoli!',
    'QR-PICKUP-002-BN-CW'
);

-- Pickup 3: SCHEDULED — NGO to pick up curry
INSERT INTO pickups (id, food_listing_id, restaurant_id, claimer_id, claimer_type, status, scheduled_pickup_time, actual_pickup_time, notes, rating, feedback, qr_code)
VALUES (
    'f0000000-0000-0000-0000-000000000003',
    'e0000000-0000-0000-0000-000000000004',
    'c0000000-0000-0000-0000-000000000013',
    'a0000000-0000-0000-0000-000000000021',
    'NGO',
    'SCHEDULED',
    CURRENT_TIMESTAMP + INTERVAL '1 hour',
    NULL,
    'Will send driver with insulated bags. ETA 1 hour.',
    NULL,
    NULL,
    'QR-PICKUP-003-MM-MOW'
);

-- Pickup 4: SCHEDULED — Citizen to pick up pastries
INSERT INTO pickups (id, food_listing_id, restaurant_id, claimer_id, claimer_type, status, scheduled_pickup_time, actual_pickup_time, notes, rating, feedback, qr_code)
VALUES (
    'f0000000-0000-0000-0000-000000000004',
    'e0000000-0000-0000-0000-000000000005',
    'c0000000-0000-0000-0000-000000000014',
    'a0000000-0000-0000-0000-000000000030',
    'CITIZEN',
    'SCHEDULED',
    CURRENT_TIMESTAMP + INTERVAL '2 hours',
    NULL,
    'Picking up on my way home from work.',
    NULL,
    NULL,
    'QR-PICKUP-004-LPB-AJ'
);

-- Pickup 5: IN_PROGRESS — Volunteer picking up sushi
INSERT INTO pickups (id, food_listing_id, restaurant_id, claimer_id, claimer_type, status, scheduled_pickup_time, actual_pickup_time, notes, rating, feedback, qr_code)
VALUES (
    'f0000000-0000-0000-0000-000000000005',
    'e0000000-0000-0000-0000-000000000002',
    'c0000000-0000-0000-0000-000000000011',
    'a0000000-0000-0000-0000-000000000034',
    'CITIZEN',
    'IN_PROGRESS',
    CURRENT_TIMESTAMP - INTERVAL '15 minutes',
    NULL,
    'On my way, will be there in 10 minutes. Bringing cooler bag.',
    NULL,
    NULL,
    'QR-PICKUP-005-SS-ED'
);

-- Pickup 6: CANCELLED — No-show scenario
INSERT INTO pickups (id, food_listing_id, restaurant_id, claimer_id, claimer_type, status, scheduled_pickup_time, actual_pickup_time, notes, rating, feedback, qr_code)
VALUES (
    'f0000000-0000-0000-0000-000000000006',
    'e0000000-0000-0000-0000-000000000008',
    'c0000000-0000-0000-0000-000000000011',
    'a0000000-0000-0000-0000-000000000033',
    'CITIZEN',
    'NO_SHOW',
    CURRENT_TIMESTAMP - INTERVAL '25 hours',
    NULL,
    NULL,
    1,
    'Claimer did not arrive at scheduled time. Food expired.',
    'QR-PICKUP-006-SS-DP'
);

-- ============================================================================
-- 7. NOTIFICATIONS (sample)
-- ============================================================================

INSERT INTO notifications (user_id, title, message, type, channel, is_read, metadata)
VALUES
    ('a0000000-0000-0000-0000-000000000020', 'New Food Available Near You', 'The Green Kitchen posted 8 servings of Organic Vegetable Soup. Pick up before tonight!', 'FOOD_AVAILABLE', 'PUSH', FALSE, '{"listing_id": "e0000000-0000-0000-0000-000000000001", "restaurant": "The Green Kitchen", "distance_km": 2.1}'),
    ('a0000000-0000-0000-0000-000000000021', 'Pickup Reminder', 'Your pickup at Mumbai Masala is scheduled in 1 hour. Don''t forget your insulated bags!', 'PICKUP_REMINDER', 'EMAIL', FALSE, '{"pickup_id": "f0000000-0000-0000-0000-000000000003", "restaurant": "Mumbai Masala"}'),
    ('a0000000-0000-0000-0000-000000000030', 'Pickup Confirmed', 'Your pickup of French Pastry Assortment from Le Petit Bistro has been confirmed for 2 hours from now.', 'PICKUP_CONFIRMED', 'WEBSOCKET', TRUE, '{"pickup_id": "f0000000-0000-0000-0000-000000000004", "restaurant": "Le Petit Bistro"}'),
    ('a0000000-0000-0000-0000-000000000010', 'Pickup Completed!', 'SF Community Food Bank picked up your Mixed Salad Bowls. You saved 10 meals from going to waste!', 'PICKUP_COMPLETED', 'IN_APP', TRUE, '{"pickup_id": "f0000000-0000-0000-0000-000000000001", "meals_saved": 10}'),
    ('a0000000-0000-0000-0000-000000000011', 'Listing Expired', 'Your listing "Edamame & Miso Soup" has expired without being picked up. Consider listing earlier next time.', 'LISTING_EXPIRED', 'EMAIL', TRUE, '{"listing_id": "e0000000-0000-0000-0000-000000000008"}'),
    ('a0000000-0000-0000-0000-000000000032', 'Rate Your Pickup', 'How was your experience picking up from Bella Napoli Pizzeria? Leave a rating!', 'RATING_REQUEST', 'PUSH', TRUE, '{"pickup_id": "f0000000-0000-0000-0000-000000000002", "restaurant": "Bella Napoli Pizzeria"}'),
    ('a0000000-0000-0000-0000-000000000033', 'Welcome to FoodWaste Platform!', 'Welcome David! Start by browsing available food listings near you. Together we can reduce food waste.', 'WELCOME', 'IN_APP', FALSE, '{}');

-- ============================================================================
-- 8. NOTIFICATION PREFERENCES
-- ============================================================================

INSERT INTO notification_preferences (user_id, email_enabled, push_enabled, websocket_enabled, food_available_alert, pickup_reminder, system_alerts)
VALUES
    ('a0000000-0000-0000-0000-000000000001', TRUE, TRUE, TRUE, FALSE, FALSE, TRUE),
    ('a0000000-0000-0000-0000-000000000010', TRUE, TRUE, TRUE, FALSE, TRUE, TRUE),
    ('a0000000-0000-0000-0000-000000000020', TRUE, TRUE, TRUE, TRUE, TRUE, TRUE),
    ('a0000000-0000-0000-0000-000000000021', TRUE, TRUE, FALSE, TRUE, TRUE, TRUE),
    ('a0000000-0000-0000-0000-000000000030', FALSE, TRUE, TRUE, TRUE, TRUE, FALSE),
    ('a0000000-0000-0000-0000-000000000032', TRUE, TRUE, TRUE, TRUE, TRUE, TRUE);

-- ============================================================================
-- 9. GEO LOCATIONS
-- ============================================================================

INSERT INTO geo_locations (entity_id, entity_type, latitude, longitude, address, city, state, country, postal_code)
VALUES
    -- Restaurants
    ('c0000000-0000-0000-0000-000000000010', 'RESTAURANT', 37.7897, -122.3999, '456 Market Street', 'San Francisco', 'California', 'US', '94105'),
    ('c0000000-0000-0000-0000-000000000011', 'RESTAURANT', 37.7862, -122.4140, '789 Geary Street', 'San Francisco', 'California', 'US', '94109'),
    ('c0000000-0000-0000-0000-000000000012', 'RESTAURANT', 37.7985, -122.4078, '321 Columbus Avenue', 'San Francisco', 'California', 'US', '94133'),
    ('c0000000-0000-0000-0000-000000000013', 'RESTAURANT', 37.7633, -122.4215, '555 Valencia Street', 'San Francisco', 'California', 'US', '94110'),
    ('c0000000-0000-0000-0000-000000000014', 'RESTAURANT', 37.7980, -122.4280, '888 Union Street', 'San Francisco', 'California', 'US', '94123'),
    -- NGOs
    ('a0000000-0000-0000-0000-000000000020', 'NGO', 37.7530, -122.3937, '900 Pennsylvania Avenue', 'San Francisco', 'California', 'US', '94107'),
    ('a0000000-0000-0000-0000-000000000021', 'NGO', 37.7290, -122.3872, '1375 Fairfax Avenue', 'San Francisco', 'California', 'US', '94124'),
    ('a0000000-0000-0000-0000-000000000022', 'NGO', 37.7855, -122.4110, '250 Ellis Street', 'San Francisco', 'California', 'US', '94102'),
    -- Citizens
    ('a0000000-0000-0000-0000-000000000030', 'CITIZEN', 37.7898, -122.3923, '150 Folsom Street', 'San Francisco', 'California', 'US', '94105'),
    ('a0000000-0000-0000-0000-000000000031', 'CITIZEN', 37.7745, -122.4572, '2130 Fulton Street', 'San Francisco', 'California', 'US', '94117'),
    ('a0000000-0000-0000-0000-000000000032', 'CITIZEN', 37.7516, -122.4287, '3801 24th Street', 'San Francisco', 'California', 'US', '94114');

-- ============================================================================
-- 10. FOOD SAVED METRICS (last 7 days for restaurants)
-- ============================================================================

INSERT INTO food_saved_metrics (restaurant_id, date, total_listings, total_claimed, total_picked_up, total_expired, food_saved_kg, co2_saved_kg, meals_provided, monetary_value_saved)
VALUES
    -- The Green Kitchen — last 7 days
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '6 days', 3, 3, 2, 1, 8.5, 22.1, 17, 102.00),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '5 days', 4, 4, 4, 0, 12.0, 31.2, 24, 144.00),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '4 days', 2, 2, 2, 0, 6.3, 16.4, 13, 75.60),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '3 days', 5, 4, 4, 1, 14.0, 36.4, 28, 168.00),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '2 days', 3, 3, 3, 0, 9.8, 25.5, 20, 117.60),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '1 day',  4, 3, 3, 1, 11.2, 29.1, 22, 134.40),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE,                      2, 1, 0, 0, 0.0, 0.0, 0, 0.00),
    -- Bella Napoli — last 7 days
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '6 days', 4, 4, 3, 1, 10.5, 27.3, 21, 126.00),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '5 days', 5, 5, 5, 0, 18.2, 47.3, 36, 218.40),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '4 days', 3, 3, 3, 0, 11.0, 28.6, 22, 132.00),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '3 days', 4, 3, 3, 1, 13.5, 35.1, 27, 162.00),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '2 days', 6, 5, 5, 1, 20.0, 52.0, 40, 240.00),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '1 day',  3, 3, 2, 0, 8.0, 20.8, 16, 96.00),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE,                      2, 1, 1, 0, 5.0, 13.0, 10, 60.00);

-- ============================================================================
-- 11. PLATFORM METRICS (last 7 days)
-- ============================================================================

INSERT INTO platform_metrics (date, total_users, active_users, total_restaurants, total_ngos, total_listings, total_pickups, total_food_saved_kg, total_meals_provided)
VALUES
    (CURRENT_DATE - INTERVAL '6 days', 142, 85, 48, 12, 320, 245, 1850.5, 3701),
    (CURRENT_DATE - INTERVAL '5 days', 145, 91, 48, 12, 335, 260, 1920.0, 3840),
    (CURRENT_DATE - INTERVAL '4 days', 147, 78, 49, 12, 310, 238, 1780.3, 3561),
    (CURRENT_DATE - INTERVAL '3 days', 150, 95, 49, 13, 350, 275, 2010.0, 4020),
    (CURRENT_DATE - INTERVAL '2 days', 153, 102, 50, 13, 365, 290, 2150.8, 4302),
    (CURRENT_DATE - INTERVAL '1 day',  155, 88, 50, 13, 340, 268, 1980.5, 3961),
    (CURRENT_DATE,                      157, 72, 50, 13, 180, 135, 1050.0, 2100);

-- ============================================================================
-- 12. RESTAURANT ANALYTICS (weekly and monthly for 2 restaurants)
-- ============================================================================

INSERT INTO restaurant_analytics (restaurant_id, period, period_start, period_end, waste_generated, waste_redirected, waste_reduction_percent, top_categories, peak_waste_hours)
VALUES
    -- The Green Kitchen — weekly
    ('c0000000-0000-0000-0000-000000000010', 'WEEKLY', CURRENT_DATE - INTERVAL '13 days', CURRENT_DATE - INTERVAL '7 days', 87.5, 62.0, 70.9, '["PREPARED_MEALS", "FRUITS_VEGETABLES", "BAKED_GOODS"]', '[{"hour": 14, "waste_kg": 8.2}, {"hour": 21, "waste_kg": 12.5}, {"hour": 22, "waste_kg": 15.1}]'),
    ('c0000000-0000-0000-0000-000000000010', 'WEEKLY', CURRENT_DATE - INTERVAL '6 days', CURRENT_DATE, 91.3, 61.8, 67.7, '["PREPARED_MEALS", "BAKED_GOODS", "DAIRY"]', '[{"hour": 14, "waste_kg": 7.8}, {"hour": 21, "waste_kg": 13.1}, {"hour": 22, "waste_kg": 14.8}]'),
    -- The Green Kitchen — monthly
    ('c0000000-0000-0000-0000-000000000010', 'MONTHLY', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE, 375.0, 265.0, 70.7, '["PREPARED_MEALS", "FRUITS_VEGETABLES", "BAKED_GOODS"]', '[{"hour": 14, "waste_kg": 35.0}, {"hour": 21, "waste_kg": 52.0}, {"hour": 22, "waste_kg": 61.0}]'),
    -- Bella Napoli — weekly
    ('c0000000-0000-0000-0000-000000000012', 'WEEKLY', CURRENT_DATE - INTERVAL '13 days', CURRENT_DATE - INTERVAL '7 days', 105.0, 82.0, 78.1, '["BAKED_GOODS", "PREPARED_MEALS", "DAIRY"]', '[{"hour": 13, "waste_kg": 10.5}, {"hour": 20, "waste_kg": 18.3}, {"hour": 22, "waste_kg": 22.0}]'),
    ('c0000000-0000-0000-0000-000000000012', 'WEEKLY', CURRENT_DATE - INTERVAL '6 days', CURRENT_DATE, 112.0, 86.2, 77.0, '["BAKED_GOODS", "PREPARED_MEALS", "RAW_INGREDIENTS"]', '[{"hour": 13, "waste_kg": 11.2}, {"hour": 20, "waste_kg": 19.5}, {"hour": 22, "waste_kg": 20.8}]'),
    -- Bella Napoli — monthly
    ('c0000000-0000-0000-0000-000000000012', 'MONTHLY', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE, 450.0, 345.0, 76.7, '["BAKED_GOODS", "PREPARED_MEALS", "DAIRY"]', '[{"hour": 13, "waste_kg": 45.0}, {"hour": 20, "waste_kg": 78.0}, {"hour": 22, "waste_kg": 88.0}]');

-- ============================================================================
-- 13. WASTE PREDICTIONS (next 7 days for 2 restaurants)
-- ============================================================================

INSERT INTO waste_predictions (restaurant_id, predicted_date, predicted_waste_kg, actual_waste_kg, confidence, category, factors, model_version)
VALUES
    -- The Green Kitchen — predictions
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE,                      13.2, NULL, 0.87, 'PREPARED_MEALS', '{"day_of_week": "Saturday", "weather": "sunny", "nearby_events": 0, "historical_avg": 12.8}', 'v2.1.0'),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE + INTERVAL '1 day',  10.5, NULL, 0.82, 'PREPARED_MEALS', '{"day_of_week": "Sunday", "weather": "cloudy", "nearby_events": 0, "historical_avg": 10.2}', 'v2.1.0'),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE + INTERVAL '2 days', 14.8, NULL, 0.79, 'PREPARED_MEALS', '{"day_of_week": "Monday", "weather": "rainy", "nearby_events": 1, "historical_avg": 13.5}', 'v2.1.0'),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE + INTERVAL '3 days', 12.1, NULL, 0.75, 'PREPARED_MEALS', '{"day_of_week": "Tuesday", "weather": "sunny", "nearby_events": 0, "historical_avg": 12.0}', 'v2.1.0'),
    -- Bella Napoli — predictions
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE,                      16.5, NULL, 0.91, 'BAKED_GOODS', '{"day_of_week": "Saturday", "weather": "sunny", "nearby_events": 2, "historical_avg": 15.8}', 'v2.1.0'),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE + INTERVAL '1 day',  11.0, NULL, 0.85, 'BAKED_GOODS', '{"day_of_week": "Sunday", "weather": "cloudy", "nearby_events": 0, "historical_avg": 10.5}', 'v2.1.0'),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE + INTERVAL '2 days', 18.3, NULL, 0.80, 'BAKED_GOODS', '{"day_of_week": "Monday", "weather": "rainy", "nearby_events": 1, "historical_avg": 17.0}', 'v2.1.0'),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE + INTERVAL '3 days', 14.7, NULL, 0.77, 'BAKED_GOODS', '{"day_of_week": "Tuesday", "weather": "sunny", "nearby_events": 0, "historical_avg": 14.2}', 'v2.1.0');

-- ============================================================================
-- 14. WASTE HISTORY (last 14 days for 2 restaurants)
-- ============================================================================

INSERT INTO waste_history (restaurant_id, date, waste_kg, category, day_of_week, month, is_holiday, weather_condition, special_event)
VALUES
    -- The Green Kitchen — 14 days of history
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '13 days', 11.5, 'PREPARED_MEALS', 6, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '13 days'), FALSE, 'sunny', NULL),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '12 days', 9.8, 'PREPARED_MEALS', 0, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '12 days'), FALSE, 'cloudy', NULL),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '11 days', 13.2, 'PREPARED_MEALS', 1, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '11 days'), FALSE, 'rainy', NULL),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '10 days', 12.0, 'PREPARED_MEALS', 2, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '10 days'), FALSE, 'sunny', NULL),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '9 days', 14.5, 'PREPARED_MEALS', 3, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '9 days'), FALSE, 'sunny', 'Farmers Market'),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '8 days', 13.8, 'PREPARED_MEALS', 4, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '8 days'), FALSE, 'cloudy', NULL),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '7 days', 15.0, 'PREPARED_MEALS', 5, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '7 days'), FALSE, 'sunny', NULL),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '6 days', 12.5, 'PREPARED_MEALS', 6, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '6 days'), FALSE, 'rainy', NULL),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '5 days', 10.0, 'PREPARED_MEALS', 0, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '5 days'), FALSE, 'sunny', NULL),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '4 days', 11.3, 'PREPARED_MEALS', 1, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '4 days'), FALSE, 'cloudy', NULL),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '3 days', 14.0, 'PREPARED_MEALS', 2, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '3 days'), FALSE, 'sunny', NULL),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '2 days', 12.8, 'PREPARED_MEALS', 3, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '2 days'), FALSE, 'sunny', NULL),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE - INTERVAL '1 day', 13.5, 'PREPARED_MEALS', 4, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 day'), FALSE, 'cloudy', NULL),
    ('c0000000-0000-0000-0000-000000000010', CURRENT_DATE, 12.2, 'PREPARED_MEALS', 5, EXTRACT(MONTH FROM CURRENT_DATE), FALSE, 'sunny', NULL),
    -- Bella Napoli — 14 days of history
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '13 days', 14.0, 'BAKED_GOODS', 6, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '13 days'), FALSE, 'sunny', NULL),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '12 days', 11.5, 'BAKED_GOODS', 0, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '12 days'), FALSE, 'cloudy', NULL),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '11 days', 16.8, 'BAKED_GOODS', 1, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '11 days'), FALSE, 'rainy', NULL),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '10 days', 15.2, 'BAKED_GOODS', 2, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '10 days'), FALSE, 'sunny', NULL),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '9 days', 18.0, 'BAKED_GOODS', 3, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '9 days'), FALSE, 'sunny', 'Wine Festival'),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '8 days', 16.5, 'BAKED_GOODS', 4, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '8 days'), FALSE, 'cloudy', NULL),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '7 days', 19.0, 'BAKED_GOODS', 5, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '7 days'), FALSE, 'sunny', NULL),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '6 days', 15.0, 'BAKED_GOODS', 6, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '6 days'), FALSE, 'rainy', NULL),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '5 days', 13.0, 'BAKED_GOODS', 0, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '5 days'), FALSE, 'sunny', NULL),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '4 days', 14.5, 'BAKED_GOODS', 1, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '4 days'), FALSE, 'cloudy', NULL),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '3 days', 17.2, 'BAKED_GOODS', 2, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '3 days'), FALSE, 'sunny', NULL),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '2 days', 15.8, 'BAKED_GOODS', 3, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '2 days'), FALSE, 'sunny', NULL),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE - INTERVAL '1 day', 16.0, 'BAKED_GOODS', 4, EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 day'), FALSE, 'cloudy', NULL),
    ('c0000000-0000-0000-0000-000000000012', CURRENT_DATE, 15.5, 'BAKED_GOODS', 5, EXTRACT(MONTH FROM CURRENT_DATE), FALSE, 'sunny', 'Local Food Fair');

-- ============================================================================
-- End of seed data
-- ============================================================================
