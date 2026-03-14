"""
LSTM Model Definition for Food Waste Prediction.

Architecture:
    Input -> LSTM(64) -> Dropout(0.2) -> LSTM(32) -> Dropout(0.2)
          -> Dense(16, relu) -> Dense(1, linear)

Compiled with Adam optimiser, MSE loss, and MAE metric.
"""

import os
import logging
from typing import Optional, Tuple

import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers, callbacks, optimizers

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)


# --------------------------------------------------------------------------- #
# Model builder
# --------------------------------------------------------------------------- #


def build_waste_prediction_model(
    sequence_length: int = 7,
    n_features: int = 10,
    lstm_units_1: int = 64,
    lstm_units_2: int = 32,
    dropout_rate: float = 0.2,
    dense_units: int = 16,
    learning_rate: float = 1e-3,
) -> keras.Model:
    """
    Build and compile the LSTM model for time-series food waste prediction.

    Parameters
    ----------
    sequence_length : int
        Number of time-steps in each input sequence (look-back window).
    n_features : int
        Number of features at each time-step.
    lstm_units_1 : int
        Units in the first LSTM layer.
    lstm_units_2 : int
        Units in the second LSTM layer.
    dropout_rate : float
        Dropout probability after each LSTM layer.
    dense_units : int
        Units in the hidden Dense layer.
    learning_rate : float
        Adam learning-rate.

    Returns
    -------
    keras.Model
        Compiled Keras model ready for training.
    """
    model = keras.Sequential(
        [
            layers.Input(shape=(sequence_length, n_features), name="input_sequence"),
            # First LSTM layer -- return sequences for stacking
            layers.LSTM(
                lstm_units_1,
                return_sequences=True,
                name="lstm_1",
            ),
            layers.Dropout(dropout_rate, name="dropout_1"),
            # Second LSTM layer -- return final hidden state only
            layers.LSTM(
                lstm_units_2,
                return_sequences=False,
                name="lstm_2",
            ),
            layers.Dropout(dropout_rate, name="dropout_2"),
            # Fully connected head
            layers.Dense(dense_units, activation="relu", name="dense_hidden"),
            layers.Dense(1, activation="linear", name="output"),
        ],
        name="FoodWasteLSTM",
    )

    optimizer = optimizers.Adam(learning_rate=learning_rate)

    model.compile(
        optimizer=optimizer,
        loss="mse",
        metrics=["mae"],
    )

    logger.info("Model compiled successfully.")
    model.summary(print_fn=logger.info)
    return model


# --------------------------------------------------------------------------- #
# Callbacks
# --------------------------------------------------------------------------- #


def get_callbacks(
    model_dir: str = "models/checkpoints",
    log_dir: str = "logs/fit",
    patience: int = 10,
    min_delta: float = 1e-4,
) -> list:
    """
    Return a list of Keras callbacks for training.

    Includes:
        - EarlyStopping on val_loss
        - ModelCheckpoint (best val_loss)
        - TensorBoard logging
        - ReduceLROnPlateau
    """
    os.makedirs(model_dir, exist_ok=True)
    os.makedirs(log_dir, exist_ok=True)

    early_stop = callbacks.EarlyStopping(
        monitor="val_loss",
        patience=patience,
        min_delta=min_delta,
        restore_best_weights=True,
        verbose=1,
    )

    checkpoint = callbacks.ModelCheckpoint(
        filepath=os.path.join(model_dir, "best_model.keras"),
        monitor="val_loss",
        save_best_only=True,
        verbose=1,
    )

    tensorboard = callbacks.TensorBoard(
        log_dir=log_dir,
        histogram_freq=1,
        write_graph=True,
    )

    reduce_lr = callbacks.ReduceLROnPlateau(
        monitor="val_loss",
        factor=0.5,
        patience=5,
        min_lr=1e-6,
        verbose=1,
    )

    return [early_stop, checkpoint, tensorboard, reduce_lr]


# --------------------------------------------------------------------------- #
# Save / Load helpers
# --------------------------------------------------------------------------- #


def save_model(model: keras.Model, path: str = "models/saved_model") -> str:
    """Save model in TensorFlow SavedModel format."""
    os.makedirs(path, exist_ok=True)
    model.save(path)
    logger.info("Model saved to %s", path)
    return path


def load_model(path: str = "models/saved_model") -> keras.Model:
    """Load a previously saved model."""
    model = keras.models.load_model(path)
    logger.info("Model loaded from %s", path)
    return model


# --------------------------------------------------------------------------- #
# Quick sanity check
# --------------------------------------------------------------------------- #

if __name__ == "__main__":
    model = build_waste_prediction_model()
    print(f"\nTotal parameters: {model.count_params():,}")
    # Smoke test with random data
    import numpy as np

    dummy_X = np.random.rand(16, 7, 10).astype("float32")
    preds = model.predict(dummy_X, verbose=0)
    print(f"Dummy predictions shape: {preds.shape}")
