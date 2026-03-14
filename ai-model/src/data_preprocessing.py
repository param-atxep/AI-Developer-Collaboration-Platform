"""
Data Preprocessing Module for Food Waste Prediction.

Handles CSV loading, cleaning, feature engineering, normalization,
train/test splitting, and scaler persistence for the LSTM model.
"""

import os
import logging
from typing import Tuple, Optional, List, Dict, Any

import numpy as np
import pandas as pd
from sklearn.preprocessing import MinMaxScaler, LabelEncoder
from sklearn.model_selection import train_test_split
import joblib

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

# --------------------------------------------------------------------------- #
# Constants
# --------------------------------------------------------------------------- #

# US federal holidays (month, day) -- simplified static list
US_HOLIDAYS: List[Tuple[int, int]] = [
    (1, 1),    # New Year's Day
    (1, 15),   # MLK Day (approx)
    (2, 19),   # Presidents' Day (approx)
    (5, 27),   # Memorial Day (approx)
    (7, 4),    # Independence Day
    (9, 2),    # Labor Day (approx)
    (10, 14),  # Columbus Day (approx)
    (11, 11),  # Veterans Day
    (11, 28),  # Thanksgiving (approx)
    (12, 25),  # Christmas
]

FOOD_CATEGORIES: List[str] = [
    "PREPARED_MEALS",
    "BAKERY",
    "PRODUCE",
    "DAIRY",
    "BEVERAGES",
]

WEATHER_CONDITIONS: List[str] = [
    "sunny",
    "cloudy",
    "rainy",
    "stormy",
    "snowy",
]

SEQUENCE_LENGTH: int = 7  # 7-day look-back window

# --------------------------------------------------------------------------- #
# Loading
# --------------------------------------------------------------------------- #


def load_data(filepath: str) -> pd.DataFrame:
    """Load raw CSV data and perform initial type casting."""
    logger.info("Loading data from %s", filepath)
    df = pd.read_csv(filepath, parse_dates=["date"])
    logger.info("Loaded %d rows, %d columns", len(df), len(df.columns))
    return df


# --------------------------------------------------------------------------- #
# Cleaning
# --------------------------------------------------------------------------- #


def clean_data(df: pd.DataFrame) -> pd.DataFrame:
    """Drop duplicates, handle missing values, remove outliers."""
    initial_len = len(df)
    df = df.drop_duplicates()

    # Forward-fill then back-fill numeric columns per restaurant
    numeric_cols = df.select_dtypes(include=[np.number]).columns.tolist()
    df[numeric_cols] = df.groupby("restaurant_id")[numeric_cols].transform(
        lambda grp: grp.fillna(method="ffill").fillna(method="bfill")
    )

    # Drop any remaining rows with NaN in critical columns
    critical = ["date", "restaurant_id", "waste_kg", "category"]
    df = df.dropna(subset=critical)

    # Remove extreme outliers (waste_kg > mean + 4*std per restaurant)
    stats = df.groupby("restaurant_id")["waste_kg"].transform(
        lambda x: (x - x.mean()).abs() / (x.std() + 1e-8)
    )
    df = df[stats < 4.0].copy()

    logger.info("Cleaning: %d -> %d rows", initial_len, len(df))
    return df.reset_index(drop=True)


# --------------------------------------------------------------------------- #
# Feature Engineering
# --------------------------------------------------------------------------- #


def _is_holiday(date: pd.Timestamp) -> int:
    """Return 1 if the date falls on (or within 1 day of) a US holiday."""
    for m, d in US_HOLIDAYS:
        try:
            holiday_date = pd.Timestamp(year=date.year, month=m, day=d)
            if abs((date - holiday_date).days) <= 1:
                return 1
        except ValueError:
            continue
    return 0


def engineer_features(df: pd.DataFrame) -> pd.DataFrame:
    """Create temporal, categorical, and lag features."""
    df = df.sort_values(["restaurant_id", "date"]).copy()

    # --- Temporal features ---
    df["day_of_week"] = df["date"].dt.dayofweek          # 0=Mon ... 6=Sun
    df["month"] = df["date"].dt.month
    df["day_of_month"] = df["date"].dt.day
    df["week_of_year"] = df["date"].dt.isocalendar().week.astype(int)
    df["is_weekend"] = (df["day_of_week"] >= 5).astype(int)
    df["is_holiday"] = df["date"].apply(_is_holiday)

    # --- Rolling / lag features (per restaurant + category) ---
    group_cols = ["restaurant_id", "category"]
    df["rolling_avg_7d"] = (
        df.groupby(group_cols)["waste_kg"]
        .transform(lambda x: x.rolling(window=7, min_periods=1).mean())
    )
    df["rolling_std_7d"] = (
        df.groupby(group_cols)["waste_kg"]
        .transform(lambda x: x.rolling(window=7, min_periods=1).std().fillna(0))
    )
    df["lag_1d"] = df.groupby(group_cols)["waste_kg"].shift(1)
    df["lag_7d"] = df.groupby(group_cols)["waste_kg"].shift(7)

    # Fill lag NaNs with rolling average (safe fallback)
    df["lag_1d"] = df["lag_1d"].fillna(df["rolling_avg_7d"])
    df["lag_7d"] = df["lag_7d"].fillna(df["rolling_avg_7d"])

    # --- Encode categoricals ---
    df["category_encoded"] = LabelEncoder().fit_transform(df["category"])

    if "weather" in df.columns:
        df["weather_encoded"] = LabelEncoder().fit_transform(df["weather"])
    else:
        df["weather_encoded"] = 0  # default if weather data absent

    logger.info("Feature engineering complete. Shape: %s", df.shape)
    return df


# --------------------------------------------------------------------------- #
# Normalization & Splitting
# --------------------------------------------------------------------------- #

# Features fed into the LSTM (order matters for sequence building)
FEATURE_COLUMNS: List[str] = [
    "day_of_week",
    "month",
    "is_weekend",
    "is_holiday",
    "rolling_avg_7d",
    "rolling_std_7d",
    "lag_1d",
    "lag_7d",
    "category_encoded",
    "weather_encoded",
]

TARGET_COLUMN: str = "waste_kg"


def normalize_data(
    df: pd.DataFrame,
    feature_cols: Optional[List[str]] = None,
    target_col: str = TARGET_COLUMN,
    scaler_path: Optional[str] = None,
) -> Tuple[pd.DataFrame, MinMaxScaler, MinMaxScaler]:
    """Min-Max scale features and target independently. Optionally save scalers."""
    feature_cols = feature_cols or FEATURE_COLUMNS

    feature_scaler = MinMaxScaler()
    target_scaler = MinMaxScaler()

    df[feature_cols] = feature_scaler.fit_transform(df[feature_cols])
    df[[target_col]] = target_scaler.fit_transform(df[[target_col]])

    if scaler_path:
        os.makedirs(scaler_path, exist_ok=True)
        joblib.dump(feature_scaler, os.path.join(scaler_path, "feature_scaler.pkl"))
        joblib.dump(target_scaler, os.path.join(scaler_path, "target_scaler.pkl"))
        logger.info("Scalers saved to %s", scaler_path)

    return df, feature_scaler, target_scaler


def create_sequences(
    df: pd.DataFrame,
    feature_cols: Optional[List[str]] = None,
    target_col: str = TARGET_COLUMN,
    seq_length: int = SEQUENCE_LENGTH,
) -> Tuple[np.ndarray, np.ndarray]:
    """
    Build (X, y) arrays where X has shape (samples, seq_length, n_features)
    and y has shape (samples, 1).

    Sequences are created per restaurant to avoid data leakage across entities.
    """
    feature_cols = feature_cols or FEATURE_COLUMNS
    X_all, y_all = [], []

    for _, group in df.groupby("restaurant_id"):
        features = group[feature_cols].values
        target = group[target_col].values

        for i in range(seq_length, len(features)):
            X_all.append(features[i - seq_length: i])
            y_all.append(target[i])

    X = np.array(X_all, dtype=np.float32)
    y = np.array(y_all, dtype=np.float32).reshape(-1, 1)
    logger.info("Sequences created: X=%s, y=%s", X.shape, y.shape)
    return X, y


def split_data(
    X: np.ndarray,
    y: np.ndarray,
    test_size: float = 0.2,
    random_state: int = 42,
) -> Tuple[np.ndarray, np.ndarray, np.ndarray, np.ndarray]:
    """80/20 train-test split preserving temporal order via shuffle=False."""
    split_idx = int(len(X) * (1 - test_size))
    X_train, X_test = X[:split_idx], X[split_idx:]
    y_train, y_test = y[:split_idx], y[split_idx:]
    logger.info(
        "Split: train=%d, test=%d (%.0f%%/%.0f%%)",
        len(X_train), len(X_test),
        (1 - test_size) * 100, test_size * 100,
    )
    return X_train, X_test, y_train, y_test


# --------------------------------------------------------------------------- #
# Full pipeline convenience
# --------------------------------------------------------------------------- #


def preprocess_pipeline(
    csv_path: str,
    scaler_path: str = "models/scalers",
    test_size: float = 0.2,
) -> Dict[str, Any]:
    """End-to-end preprocessing pipeline returning train/test arrays + scalers."""
    df = load_data(csv_path)
    df = clean_data(df)
    df = engineer_features(df)
    df, feat_scaler, tgt_scaler = normalize_data(df, scaler_path=scaler_path)
    X, y = create_sequences(df)
    X_train, X_test, y_train, y_test = split_data(X, y, test_size=test_size)

    return {
        "X_train": X_train,
        "X_test": X_test,
        "y_train": y_train,
        "y_test": y_test,
        "feature_scaler": feat_scaler,
        "target_scaler": tgt_scaler,
        "feature_columns": FEATURE_COLUMNS,
        "sequence_length": SEQUENCE_LENGTH,
    }


# --------------------------------------------------------------------------- #
# CLI entry-point
# --------------------------------------------------------------------------- #

if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="Preprocess food waste data")
    parser.add_argument("--input", required=True, help="Path to raw CSV")
    parser.add_argument("--scaler-dir", default="models/scalers", help="Dir to save scalers")
    args = parser.parse_args()

    result = preprocess_pipeline(args.input, scaler_path=args.scaler_dir)
    print(f"X_train: {result['X_train'].shape}")
    print(f"X_test:  {result['X_test'].shape}")
    print(f"y_train: {result['y_train'].shape}")
    print(f"y_test:  {result['y_test'].shape}")
