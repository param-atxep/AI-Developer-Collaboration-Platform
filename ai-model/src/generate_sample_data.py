"""
Generate Realistic Synthetic Training Data for Food Waste Prediction.

Produces a CSV with the following columns:
    date, restaurant_id, restaurant_name, category, waste_kg,
    weather, temperature_c, customers, revenue

Patterns encoded:
    - Higher waste on weekends (Fri-Sun)
    - Lower waste on Mondays
    - Seasonal variations (higher in summer, lower in winter)
    - Holiday spikes
    - Per-restaurant and per-category baselines
    - Weather influence (rainy/stormy -> less foot traffic -> less waste in some categories)

10 restaurants x 365 days x 5 categories = up to 18,250 rows.
"""

import os
import argparse
import logging
from typing import List

import numpy as np
import pandas as pd

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

# --------------------------------------------------------------------------- #
# Configuration
# --------------------------------------------------------------------------- #

SEED = 42
N_RESTAURANTS = 10
DAYS = 365
START_DATE = "2023-01-01"

CATEGORIES: List[str] = [
    "PREPARED_MEALS",
    "BAKERY",
    "PRODUCE",
    "DAIRY",
    "BEVERAGES",
]

WEATHER_OPTIONS: List[str] = ["sunny", "cloudy", "rainy", "stormy", "snowy"]

# Seasonal weather probability weights (spring, summer, autumn, winter)
WEATHER_SEASONAL: dict = {
    "spring": [0.35, 0.30, 0.25, 0.05, 0.05],
    "summer": [0.50, 0.25, 0.15, 0.08, 0.02],
    "autumn": [0.20, 0.35, 0.30, 0.10, 0.05],
    "winter": [0.15, 0.25, 0.20, 0.10, 0.30],
}

# US holidays (month, day) for spike injection
HOLIDAYS = [
    (1, 1), (2, 14), (3, 17), (5, 27), (7, 4),
    (9, 2), (10, 31), (11, 28), (12, 25), (12, 31),
]

RESTAURANT_NAMES = [
    "Green Plate Bistro",
    "Urban Harvest Kitchen",
    "The Rustic Table",
    "Fresh Fusion Cafe",
    "Harbor View Grill",
    "Sunrise Deli & Bakery",
    "Farm to Fork Eatery",
    "Central Market Diner",
    "Lakeside Provisions",
    "Golden Spoon Tavern",
]

# Per-category base waste (kg) -- represents a 'typical' mid-week day
CATEGORY_BASE_WASTE: dict = {
    "PREPARED_MEALS": 15.0,
    "BAKERY": 8.0,
    "PRODUCE": 12.0,
    "DAIRY": 5.0,
    "BEVERAGES": 3.0,
}

# Per-category daily noise std
CATEGORY_NOISE_STD: dict = {
    "PREPARED_MEALS": 3.0,
    "BAKERY": 2.0,
    "PRODUCE": 2.5,
    "DAIRY": 1.2,
    "BEVERAGES": 0.8,
}


# --------------------------------------------------------------------------- #
# Helpers
# --------------------------------------------------------------------------- #


def _season(month: int) -> str:
    if month in (3, 4, 5):
        return "spring"
    if month in (6, 7, 8):
        return "summer"
    if month in (9, 10, 11):
        return "autumn"
    return "winter"


def _seasonal_multiplier(month: int) -> float:
    """Summer months see ~20% more waste; winter ~15% less."""
    seasonal = {
        1: 0.85, 2: 0.87, 3: 0.92, 4: 0.97,
        5: 1.02, 6: 1.10, 7: 1.18, 8: 1.15,
        9: 1.05, 10: 0.98, 11: 0.93, 12: 0.90,
    }
    return seasonal.get(month, 1.0)


def _day_of_week_multiplier(dow: int) -> float:
    """
    0=Mon ... 6=Sun
    Monday is low, Friday-Sunday higher.
    """
    factors = {
        0: 0.80,  # Monday
        1: 0.90,
        2: 0.95,
        3: 1.00,
        4: 1.10,  # Friday
        5: 1.25,  # Saturday
        6: 1.20,  # Sunday
    }
    return factors.get(dow, 1.0)


def _is_holiday(date: pd.Timestamp) -> bool:
    for m, d in HOLIDAYS:
        try:
            hd = pd.Timestamp(year=date.year, month=m, day=d)
            if abs((date - hd).days) <= 1:
                return True
        except ValueError:
            continue
    return False


def _weather_for_date(date: pd.Timestamp, rng: np.random.Generator) -> str:
    season = _season(date.month)
    probs = WEATHER_SEASONAL[season]
    return rng.choice(WEATHER_OPTIONS, p=probs)


def _temperature(date: pd.Timestamp, weather: str, rng: np.random.Generator) -> float:
    """Generate a plausible temperature (C) based on month and weather."""
    # Monthly average temps (Northern Hemisphere mid-latitude)
    monthly_avg = {
        1: 2, 2: 3, 3: 8, 4: 13, 5: 18, 6: 23,
        7: 26, 8: 25, 9: 20, 10: 14, 11: 8, 12: 3,
    }
    base = monthly_avg.get(date.month, 15)
    # Weather adjustments
    adj = {"sunny": 3, "cloudy": 0, "rainy": -2, "stormy": -4, "snowy": -6}
    return round(base + adj.get(weather, 0) + rng.normal(0, 2), 1)


# --------------------------------------------------------------------------- #
# Main generator
# --------------------------------------------------------------------------- #


def generate_data(
    n_restaurants: int = N_RESTAURANTS,
    days: int = DAYS,
    start_date: str = START_DATE,
    seed: int = SEED,
) -> pd.DataFrame:
    """Generate synthetic food waste data."""
    rng = np.random.default_rng(seed)
    dates = pd.date_range(start=start_date, periods=days, freq="D")

    # Per-restaurant scaling factor (some restaurants are bigger)
    restaurant_scales = rng.uniform(0.6, 1.5, size=n_restaurants)

    rows = []

    for r_idx in range(n_restaurants):
        restaurant_id = f"rest_{r_idx + 1:03d}"
        restaurant_name = RESTAURANT_NAMES[r_idx] if r_idx < len(RESTAURANT_NAMES) else f"Restaurant {r_idx + 1}"
        r_scale = restaurant_scales[r_idx]

        for date in dates:
            dow = date.dayofweek
            month = date.month
            holiday = _is_holiday(date)
            weather = _weather_for_date(date, rng)
            temp = _temperature(date, weather, rng)

            # Customer count (influences waste proportionally)
            base_customers = int(120 * r_scale)
            customer_mult = _day_of_week_multiplier(dow) * _seasonal_multiplier(month)
            if holiday:
                customer_mult *= rng.uniform(1.2, 1.6)
            if weather in ("stormy", "snowy"):
                customer_mult *= rng.uniform(0.6, 0.8)
            elif weather == "rainy":
                customer_mult *= rng.uniform(0.8, 0.95)

            customers = max(10, int(base_customers * customer_mult + rng.normal(0, 10)))
            revenue = round(customers * rng.uniform(12, 25), 2)

            for category in CATEGORIES:
                base = CATEGORY_BASE_WASTE[category] * r_scale
                noise_std = CATEGORY_NOISE_STD[category]

                waste = base
                waste *= _day_of_week_multiplier(dow)
                waste *= _seasonal_multiplier(month)

                # Holiday spike
                if holiday:
                    waste *= rng.uniform(1.3, 1.8)

                # Weather effect (stormy -> less customers -> less prepared -> less waste for some)
                if weather in ("stormy", "snowy"):
                    if category in ("PREPARED_MEALS", "BAKERY"):
                        waste *= rng.uniform(0.7, 0.9)
                    else:
                        waste *= rng.uniform(0.85, 1.0)

                # Random noise
                waste += rng.normal(0, noise_std)

                # Ensure non-negative
                waste = max(round(waste, 2), 0.1)

                rows.append({
                    "date": date,
                    "restaurant_id": restaurant_id,
                    "restaurant_name": restaurant_name,
                    "category": category,
                    "waste_kg": waste,
                    "weather": weather,
                    "temperature_c": temp,
                    "customers": customers,
                    "revenue": revenue,
                })

    df = pd.DataFrame(rows)
    logger.info("Generated %d rows for %d restaurants over %d days", len(df), n_restaurants, days)
    return df


# --------------------------------------------------------------------------- #
# CLI
# --------------------------------------------------------------------------- #

def main():
    parser = argparse.ArgumentParser(description="Generate synthetic food waste data")
    parser.add_argument(
        "--output",
        default="data/food_waste_data.csv",
        help="Output CSV path (default: data/food_waste_data.csv)",
    )
    parser.add_argument("--restaurants", type=int, default=N_RESTAURANTS)
    parser.add_argument("--days", type=int, default=DAYS)
    parser.add_argument("--seed", type=int, default=SEED)
    args = parser.parse_args()

    df = generate_data(
        n_restaurants=args.restaurants,
        days=args.days,
        seed=args.seed,
    )

    os.makedirs(os.path.dirname(args.output) or ".", exist_ok=True)
    df.to_csv(args.output, index=False)
    logger.info("Data saved to %s", args.output)

    # Quick summary
    print(f"\nDataset shape: {df.shape}")
    print(f"Date range: {df['date'].min().date()} to {df['date'].max().date()}")
    print(f"Restaurants: {df['restaurant_id'].nunique()}")
    print(f"Categories:  {df['category'].nunique()}")
    print(f"\nWaste (kg) summary:")
    print(df["waste_kg"].describe().to_string())
    print(f"\nWaste by category:")
    print(df.groupby("category")["waste_kg"].agg(["mean", "std", "min", "max"]).round(2).to_string())


if __name__ == "__main__":
    main()
