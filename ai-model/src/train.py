"""
Training Script for Food Waste Prediction Model.

Responsibilities:
    1. Load and preprocess data via the data_preprocessing pipeline.
    2. Build the LSTM model.
    3. Train with validation, early stopping, and learning-rate reduction.
    4. Log training metrics and curves.
    5. Save the final model (SavedModel format) and scalers.
    6. Print evaluation metrics: MAE, RMSE, R-squared.
"""

import os
import sys
import json
import time
import logging
import argparse
from datetime import datetime
from typing import Dict, Any

import numpy as np
import matplotlib
matplotlib.use("Agg")  # non-interactive backend for server environments
import matplotlib.pyplot as plt

from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score

# Local imports
from data_preprocessing import preprocess_pipeline, FEATURE_COLUMNS, SEQUENCE_LENGTH
from model import build_waste_prediction_model, get_callbacks, save_model

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler("logs/training.log", mode="a"),
    ],
)
logger = logging.getLogger(__name__)

# --------------------------------------------------------------------------- #
# Defaults
# --------------------------------------------------------------------------- #

DEFAULT_DATA_PATH = "data/food_waste_data.csv"
DEFAULT_MODEL_DIR = "models/saved_model"
DEFAULT_SCALER_DIR = "models/scalers"
DEFAULT_CHECKPOINT_DIR = "models/checkpoints"
DEFAULT_LOG_DIR = "logs/fit"
DEFAULT_METRICS_PATH = "models/training_metrics.json"

EPOCHS = 100
BATCH_SIZE = 32
VALIDATION_SPLIT = 0.15


# --------------------------------------------------------------------------- #
# Evaluation helpers
# --------------------------------------------------------------------------- #


def evaluate_model(
    model,
    X_test: np.ndarray,
    y_test: np.ndarray,
    target_scaler,
) -> Dict[str, float]:
    """Compute MAE, RMSE, and R^2 on the test set in original scale."""
    y_pred_scaled = model.predict(X_test, verbose=0)

    # Inverse transform to original units (kg)
    y_pred = target_scaler.inverse_transform(y_pred_scaled).flatten()
    y_true = target_scaler.inverse_transform(y_test).flatten()

    mae = mean_absolute_error(y_true, y_pred)
    rmse = np.sqrt(mean_squared_error(y_true, y_pred))
    r2 = r2_score(y_true, y_pred)

    metrics = {"mae_kg": round(float(mae), 4), "rmse_kg": round(float(rmse), 4), "r2": round(float(r2), 4)}
    return metrics


def plot_training_history(history, output_path: str = "logs/training_curves.png") -> None:
    """Save loss and MAE curves to disk."""
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))

    # Loss
    ax1.plot(history.history["loss"], label="Train Loss")
    ax1.plot(history.history["val_loss"], label="Val Loss")
    ax1.set_title("Model Loss (MSE)")
    ax1.set_xlabel("Epoch")
    ax1.set_ylabel("Loss")
    ax1.legend()
    ax1.grid(True, alpha=0.3)

    # MAE
    ax2.plot(history.history["mae"], label="Train MAE")
    ax2.plot(history.history["val_mae"], label="Val MAE")
    ax2.set_title("Model MAE")
    ax2.set_xlabel("Epoch")
    ax2.set_ylabel("MAE")
    ax2.legend()
    ax2.grid(True, alpha=0.3)

    plt.tight_layout()
    os.makedirs(os.path.dirname(output_path) or ".", exist_ok=True)
    plt.savefig(output_path, dpi=150)
    plt.close()
    logger.info("Training curves saved to %s", output_path)


def plot_predictions(
    y_true: np.ndarray,
    y_pred: np.ndarray,
    output_path: str = "logs/prediction_scatter.png",
) -> None:
    """Save a scatter plot of predicted vs actual waste."""
    fig, ax = plt.subplots(figsize=(7, 7))
    ax.scatter(y_true, y_pred, alpha=0.3, s=10)
    lims = [min(y_true.min(), y_pred.min()), max(y_true.max(), y_pred.max())]
    ax.plot(lims, lims, "r--", linewidth=1, label="Perfect prediction")
    ax.set_xlabel("Actual Waste (kg)")
    ax.set_ylabel("Predicted Waste (kg)")
    ax.set_title("Predicted vs Actual Food Waste")
    ax.legend()
    ax.grid(True, alpha=0.3)
    plt.tight_layout()
    os.makedirs(os.path.dirname(output_path) or ".", exist_ok=True)
    plt.savefig(output_path, dpi=150)
    plt.close()
    logger.info("Prediction scatter saved to %s", output_path)


# --------------------------------------------------------------------------- #
# Main training routine
# --------------------------------------------------------------------------- #


def train(
    data_path: str = DEFAULT_DATA_PATH,
    model_dir: str = DEFAULT_MODEL_DIR,
    scaler_dir: str = DEFAULT_SCALER_DIR,
    checkpoint_dir: str = DEFAULT_CHECKPOINT_DIR,
    log_dir: str = DEFAULT_LOG_DIR,
    metrics_path: str = DEFAULT_METRICS_PATH,
    epochs: int = EPOCHS,
    batch_size: int = BATCH_SIZE,
    validation_split: float = VALIDATION_SPLIT,
) -> Dict[str, Any]:
    """Full training pipeline: preprocess -> build -> train -> evaluate -> save."""

    start_time = time.time()
    logger.info("=" * 60)
    logger.info("FOOD WASTE PREDICTION -- TRAINING STARTED")
    logger.info("=" * 60)

    # ------------------------------------------------------------------ #
    # 1. Preprocess
    # ------------------------------------------------------------------ #
    logger.info("Step 1/5: Preprocessing data ...")
    pipeline = preprocess_pipeline(data_path, scaler_path=scaler_dir)

    X_train = pipeline["X_train"]
    X_test = pipeline["X_test"]
    y_train = pipeline["y_train"]
    y_test = pipeline["y_test"]
    target_scaler = pipeline["target_scaler"]

    n_features = X_train.shape[2]
    seq_length = X_train.shape[1]

    logger.info(
        "Data ready: X_train=%s  X_test=%s  features=%d  seq_len=%d",
        X_train.shape, X_test.shape, n_features, seq_length,
    )

    # ------------------------------------------------------------------ #
    # 2. Build model
    # ------------------------------------------------------------------ #
    logger.info("Step 2/5: Building model ...")
    model = build_waste_prediction_model(
        sequence_length=seq_length,
        n_features=n_features,
    )

    # ------------------------------------------------------------------ #
    # 3. Train
    # ------------------------------------------------------------------ #
    logger.info("Step 3/5: Training model (epochs=%d, batch=%d) ...", epochs, batch_size)
    cb = get_callbacks(
        model_dir=checkpoint_dir,
        log_dir=log_dir,
    )

    history = model.fit(
        X_train,
        y_train,
        epochs=epochs,
        batch_size=batch_size,
        validation_split=validation_split,
        callbacks=cb,
        verbose=1,
    )

    # ------------------------------------------------------------------ #
    # 4. Evaluate
    # ------------------------------------------------------------------ #
    logger.info("Step 4/5: Evaluating model ...")
    metrics = evaluate_model(model, X_test, y_test, target_scaler)

    logger.info("-" * 40)
    logger.info("  MAE  (kg): %.4f", metrics["mae_kg"])
    logger.info("  RMSE (kg): %.4f", metrics["rmse_kg"])
    logger.info("  R-squared: %.4f", metrics["r2"])
    logger.info("-" * 40)

    # Plots
    plot_training_history(history)

    y_pred_scaled = model.predict(X_test, verbose=0)
    y_pred = target_scaler.inverse_transform(y_pred_scaled).flatten()
    y_true = target_scaler.inverse_transform(y_test).flatten()
    plot_predictions(y_true, y_pred)

    # ------------------------------------------------------------------ #
    # 5. Save model & metrics
    # ------------------------------------------------------------------ #
    logger.info("Step 5/5: Saving model ...")
    save_model(model, model_dir)

    elapsed = round(time.time() - start_time, 2)
    metrics_full: Dict[str, Any] = {
        "version": "1.0.0",
        "trained_at": datetime.utcnow().isoformat() + "Z",
        "training_duration_s": elapsed,
        "epochs_completed": len(history.history["loss"]),
        "best_val_loss": round(float(min(history.history["val_loss"])), 6),
        "test_metrics": metrics,
        "data_path": data_path,
        "sequence_length": seq_length,
        "n_features": n_features,
        "feature_columns": FEATURE_COLUMNS,
    }

    os.makedirs(os.path.dirname(metrics_path) or ".", exist_ok=True)
    with open(metrics_path, "w") as f:
        json.dump(metrics_full, f, indent=2)
    logger.info("Metrics saved to %s", metrics_path)

    logger.info("=" * 60)
    logger.info("TRAINING COMPLETE in %.1fs", elapsed)
    logger.info("=" * 60)

    return metrics_full


# --------------------------------------------------------------------------- #
# CLI
# --------------------------------------------------------------------------- #

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Train the food waste prediction model")
    parser.add_argument("--data", default=DEFAULT_DATA_PATH, help="Path to CSV data")
    parser.add_argument("--model-dir", default=DEFAULT_MODEL_DIR, help="SavedModel output dir")
    parser.add_argument("--scaler-dir", default=DEFAULT_SCALER_DIR, help="Scaler output dir")
    parser.add_argument("--epochs", type=int, default=EPOCHS, help="Max epochs")
    parser.add_argument("--batch-size", type=int, default=BATCH_SIZE, help="Batch size")
    args = parser.parse_args()

    results = train(
        data_path=args.data,
        model_dir=args.model_dir,
        scaler_dir=args.scaler_dir,
        epochs=args.epochs,
        batch_size=args.batch_size,
    )

    print("\n=== Final Test Metrics ===")
    for k, v in results["test_metrics"].items():
        print(f"  {k}: {v}")
