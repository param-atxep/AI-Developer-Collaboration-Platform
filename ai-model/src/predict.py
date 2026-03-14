"""
Prediction Module for Food Waste Prediction.

Responsibilities:
    - Load the saved LSTM model and scalers.
    - Preprocess a single or batch input into the expected sequence format.
    - Return point predictions with confidence intervals.
    - Generate actionable recommendations based on predicted waste patterns.
"""

import os
import json
import logging
from typing import Dict, Any, List, Optional, Tuple

import numpy as np
import pandas as pd
import joblib
from tensorflow import keras

from data_preprocessing import (
    FEATURE_COLUMNS,
    SEQUENCE_LENGTH,
    TARGET_COLUMN,
    engineer_features,
)

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

# --------------------------------------------------------------------------- #
# Defaults
# --------------------------------------------------------------------------- #

DEFAULT_MODEL_DIR = "models/saved_model"
DEFAULT_SCALER_DIR = "models/scalers"
DEFAULT_METRICS_PATH = "models/training_metrics.json"

# Approximate multiplier for 95% confidence interval based on historical
# prediction error.  A more rigorous approach would use bootstrapping or
# Monte-Carlo dropout, but this gives a useful first-order estimate.
CI_Z_95 = 1.96


# --------------------------------------------------------------------------- #
# Predictor class
# --------------------------------------------------------------------------- #


class WastePredictor:
    """High-level prediction interface wrapping model + scalers."""

    def __init__(
        self,
        model_dir: str = DEFAULT_MODEL_DIR,
        scaler_dir: str = DEFAULT_SCALER_DIR,
        metrics_path: str = DEFAULT_METRICS_PATH,
    ) -> None:
        self.model: Optional[keras.Model] = None
        self.feature_scaler = None
        self.target_scaler = None
        self.metrics: Dict[str, Any] = {}
        self.model_dir = model_dir
        self.scaler_dir = scaler_dir
        self.metrics_path = metrics_path
        self._load()

    # ------------------------------------------------------------------ #
    # Loading
    # ------------------------------------------------------------------ #

    def _load(self) -> None:
        """Load model, scalers, and training metrics from disk."""
        # Model
        if os.path.exists(self.model_dir):
            self.model = keras.models.load_model(self.model_dir)
            logger.info("Model loaded from %s", self.model_dir)
        else:
            logger.warning("Model directory not found: %s", self.model_dir)

        # Scalers
        feat_path = os.path.join(self.scaler_dir, "feature_scaler.pkl")
        tgt_path = os.path.join(self.scaler_dir, "target_scaler.pkl")
        if os.path.exists(feat_path) and os.path.exists(tgt_path):
            self.feature_scaler = joblib.load(feat_path)
            self.target_scaler = joblib.load(tgt_path)
            logger.info("Scalers loaded from %s", self.scaler_dir)
        else:
            logger.warning("Scalers not found in %s", self.scaler_dir)

        # Metrics
        if os.path.exists(self.metrics_path):
            with open(self.metrics_path) as f:
                self.metrics = json.load(f)
            logger.info("Metrics loaded from %s", self.metrics_path)

    @property
    def is_ready(self) -> bool:
        return self.model is not None and self.feature_scaler is not None and self.target_scaler is not None

    # ------------------------------------------------------------------ #
    # Preprocessing helpers
    # ------------------------------------------------------------------ #

    def _preprocess_features(self, features: Dict[str, Any]) -> np.ndarray:
        """
        Convert a single input dict into a scaled feature vector.

        Expected keys match FEATURE_COLUMNS:
            day_of_week, month, is_weekend, is_holiday,
            rolling_avg_7d, rolling_std_7d, lag_1d, lag_7d,
            category_encoded, weather_encoded
        """
        row = [features.get(col, 0) for col in FEATURE_COLUMNS]
        arr = np.array(row, dtype=np.float32).reshape(1, -1)
        arr_scaled = self.feature_scaler.transform(arr)
        return arr_scaled.flatten()

    def _build_sequence(self, historical_features: List[Dict[str, Any]]) -> np.ndarray:
        """
        Build a (1, seq_len, n_features) array from a list of daily feature dicts.
        The list must have length >= SEQUENCE_LENGTH; only the most recent days are used.
        """
        if len(historical_features) < SEQUENCE_LENGTH:
            # Pad by repeating the earliest record
            pad_count = SEQUENCE_LENGTH - len(historical_features)
            padding = [historical_features[0]] * pad_count
            historical_features = padding + historical_features

        recent = historical_features[-SEQUENCE_LENGTH:]
        vectors = [self._preprocess_features(f) for f in recent]
        sequence = np.array(vectors, dtype=np.float32)
        return sequence.reshape(1, SEQUENCE_LENGTH, len(FEATURE_COLUMNS))

    # ------------------------------------------------------------------ #
    # Single prediction
    # ------------------------------------------------------------------ #

    def predict(
        self,
        historical_features: List[Dict[str, Any]],
        confidence: float = 0.95,
    ) -> Dict[str, Any]:
        """
        Predict food waste (kg) for the next day given a history window.

        Parameters
        ----------
        historical_features : list[dict]
            List of daily feature dicts (most-recent last).  Length should be
            >= SEQUENCE_LENGTH (7).  Each dict should contain the keys from
            FEATURE_COLUMNS.
        confidence : float
            Confidence level for the interval (default 95%).

        Returns
        -------
        dict with keys:
            predicted_waste_kg, confidence_interval, lower_bound, upper_bound
        """
        if not self.is_ready:
            raise RuntimeError("Model or scalers not loaded. Train the model first.")

        X = self._build_sequence(historical_features)
        y_pred_scaled = self.model.predict(X, verbose=0)
        y_pred = self.target_scaler.inverse_transform(y_pred_scaled).flatten()[0]

        # Confidence interval from training RMSE
        rmse = self.metrics.get("test_metrics", {}).get("rmse_kg", 2.0)
        z = CI_Z_95 if confidence == 0.95 else 1.645 if confidence == 0.90 else CI_Z_95
        margin = z * rmse

        return {
            "predicted_waste_kg": round(float(max(y_pred, 0)), 2),
            "confidence_level": confidence,
            "lower_bound": round(float(max(y_pred - margin, 0)), 2),
            "upper_bound": round(float(y_pred + margin), 2),
            "unit": "kg",
        }

    # ------------------------------------------------------------------ #
    # Batch prediction
    # ------------------------------------------------------------------ #

    def predict_batch(
        self,
        batch: List[List[Dict[str, Any]]],
        confidence: float = 0.95,
    ) -> List[Dict[str, Any]]:
        """
        Run predictions for multiple restaurants/date sequences.

        Parameters
        ----------
        batch : list[list[dict]]
            Each element is a historical_features list for one prediction.

        Returns
        -------
        list[dict]
            List of prediction results.
        """
        if not self.is_ready:
            raise RuntimeError("Model or scalers not loaded. Train the model first.")

        # Stack all sequences into a single batch tensor for efficiency
        sequences = []
        for hist in batch:
            X = self._build_sequence(hist)
            sequences.append(X[0])  # shape: (seq_len, n_features)

        X_batch = np.array(sequences, dtype=np.float32)  # (N, seq_len, n_feat)
        y_pred_scaled = self.model.predict(X_batch, verbose=0)
        y_pred = self.target_scaler.inverse_transform(y_pred_scaled).flatten()

        rmse = self.metrics.get("test_metrics", {}).get("rmse_kg", 2.0)
        z = CI_Z_95 if confidence == 0.95 else 1.645

        results = []
        for pred in y_pred:
            margin = z * rmse
            results.append({
                "predicted_waste_kg": round(float(max(pred, 0)), 2),
                "confidence_level": confidence,
                "lower_bound": round(float(max(pred - margin, 0)), 2),
                "upper_bound": round(float(pred + margin), 2),
                "unit": "kg",
            })
        return results

    # ------------------------------------------------------------------ #
    # Recommendations
    # ------------------------------------------------------------------ #

    @staticmethod
    def generate_recommendations(
        predicted_kg: float,
        category: str,
        day_of_week: int,
        historical_avg: float,
    ) -> List[Dict[str, str]]:
        """
        Generate actionable recommendations based on the predicted waste and
        contextual information.

        Returns a list of recommendation dicts with 'priority' and 'message'.
        """
        recs: List[Dict[str, str]] = []
        ratio = predicted_kg / max(historical_avg, 0.1)

        # --- High waste alert ---
        if ratio > 1.3:
            recs.append({
                "priority": "HIGH",
                "type": "REDUCE_PRODUCTION",
                "message": (
                    f"Predicted waste ({predicted_kg:.1f} kg) is {(ratio - 1) * 100:.0f}% "
                    f"above average. Consider reducing {category} production."
                ),
            })

        # --- Redistribution opportunity ---
        if predicted_kg > 5:
            recs.append({
                "priority": "MEDIUM",
                "type": "SCHEDULE_PICKUP",
                "message": (
                    f"Expected surplus of {predicted_kg:.1f} kg in {category}. "
                    "Schedule a pickup with local food banks or shelters."
                ),
            })

        # --- Weekend patterns ---
        if day_of_week >= 5 and predicted_kg > historical_avg:
            recs.append({
                "priority": "MEDIUM",
                "type": "WEEKEND_ADJUSTMENT",
                "message": (
                    "Weekend waste tends to be higher. Adjust preparation quantities "
                    "or plan promotions to reduce excess."
                ),
            })

        # --- Low-waste commendation ---
        if ratio < 0.8:
            recs.append({
                "priority": "LOW",
                "type": "POSITIVE_FEEDBACK",
                "message": (
                    f"Great job! Predicted waste ({predicted_kg:.1f} kg) is "
                    f"{(1 - ratio) * 100:.0f}% below average for {category}."
                ),
            })

        # --- Category-specific tips ---
        category_tips = {
            "PREPARED_MEALS": "Consider offering smaller portions or daily specials to clear inventory.",
            "BAKERY": "Discount day-old items or partner with a composting service.",
            "PRODUCE": "Check storage temperatures and rotate stock using FIFO.",
            "DAIRY": "Monitor expiry dates closely; offer near-expiry items at a discount.",
            "BEVERAGES": "Review ordering frequency -- beverages have longer shelf life.",
        }
        tip = category_tips.get(category)
        if tip:
            recs.append({
                "priority": "INFO",
                "type": "CATEGORY_TIP",
                "message": tip,
            })

        return recs

    # ------------------------------------------------------------------ #
    # Model info
    # ------------------------------------------------------------------ #

    def get_model_info(self) -> Dict[str, Any]:
        """Return metadata about the loaded model."""
        info: Dict[str, Any] = {
            "loaded": self.is_ready,
            "model_dir": self.model_dir,
            "scaler_dir": self.scaler_dir,
        }
        if self.metrics:
            info["version"] = self.metrics.get("version", "unknown")
            info["trained_at"] = self.metrics.get("trained_at", "unknown")
            info["test_metrics"] = self.metrics.get("test_metrics", {})
            info["sequence_length"] = self.metrics.get("sequence_length", SEQUENCE_LENGTH)
            info["n_features"] = self.metrics.get("n_features", len(FEATURE_COLUMNS))
            info["epochs_completed"] = self.metrics.get("epochs_completed", 0)
        return info


# --------------------------------------------------------------------------- #
# CLI quick-test
# --------------------------------------------------------------------------- #

if __name__ == "__main__":
    predictor = WastePredictor()

    if not predictor.is_ready:
        print("Model not trained yet. Run train.py first.")
    else:
        # Build a dummy 7-day history
        dummy_history = []
        for i in range(7):
            dummy_history.append({
                "day_of_week": i % 7,
                "month": 3,
                "is_weekend": 1 if (i % 7) >= 5 else 0,
                "is_holiday": 0,
                "rolling_avg_7d": 12.5,
                "rolling_std_7d": 2.3,
                "lag_1d": 11.0 + i * 0.5,
                "lag_7d": 13.0,
                "category_encoded": 0,
                "weather_encoded": 0,
            })

        result = predictor.predict(dummy_history)
        print("\n=== Single Prediction ===")
        for k, v in result.items():
            print(f"  {k}: {v}")

        recs = WastePredictor.generate_recommendations(
            predicted_kg=result["predicted_waste_kg"],
            category="PREPARED_MEALS",
            day_of_week=3,
            historical_avg=12.5,
        )
        print("\n=== Recommendations ===")
        for r in recs:
            print(f"  [{r['priority']}] {r['message']}")
