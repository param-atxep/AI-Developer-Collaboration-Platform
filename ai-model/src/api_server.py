"""
Flask REST API Server for Food Waste Prediction.

Endpoints:
    POST   /predict          - Single prediction
    POST   /predict/batch    - Batch predictions
    GET    /model/info       - Model version and metrics
    POST   /model/retrain    - Trigger retraining
    GET    /health           - Health check

Runs on port 5000. All responses are JSON.
"""

import os
import sys
import logging
import threading
from datetime import datetime
from typing import Any, Dict

from flask import Flask, request, jsonify, Response

# Ensure local imports resolve when running from project root
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from predict import WastePredictor
from data_preprocessing import FEATURE_COLUMNS, SEQUENCE_LENGTH

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
)
logger = logging.getLogger(__name__)

# --------------------------------------------------------------------------- #
# App initialisation
# --------------------------------------------------------------------------- #

app = Flask(__name__)
app.config["JSON_SORT_KEYS"] = False

# Global predictor instance (loaded once at startup)
predictor: WastePredictor | None = None

# Track retraining state
_retrain_lock = threading.Lock()
_retrain_status: Dict[str, Any] = {"running": False, "last_run": None, "result": None}


def _init_predictor() -> None:
    """Load (or reload) the predictor from disk."""
    global predictor
    model_dir = os.environ.get("MODEL_DIR", "models/saved_model")
    scaler_dir = os.environ.get("SCALER_DIR", "models/scalers")
    metrics_path = os.environ.get("METRICS_PATH", "models/training_metrics.json")
    predictor = WastePredictor(
        model_dir=model_dir,
        scaler_dir=scaler_dir,
        metrics_path=metrics_path,
    )


_init_predictor()


# --------------------------------------------------------------------------- #
# Helpers
# --------------------------------------------------------------------------- #


def _error(message: str, status: int = 400) -> tuple:
    return jsonify({"error": message}), status


def _validate_features(features: dict) -> str | None:
    """Return an error message if required feature keys are missing, else None."""
    required = set(FEATURE_COLUMNS)
    provided = set(features.keys())
    missing = required - provided
    if missing:
        return f"Missing feature keys: {sorted(missing)}"
    return None


def _build_history_from_request(data: dict) -> list:
    """
    Accept either:
      - ``historical_features``: a pre-built list of daily feature dicts, OR
      - ``features``: a single-day dict that gets replicated into a 7-day window
        (convenient for quick tests).
    """
    if "historical_features" in data:
        return data["historical_features"]

    # Single-feature shorthand: replicate to fill the sequence window
    features = data.get("features", {})
    return [features] * SEQUENCE_LENGTH


# --------------------------------------------------------------------------- #
# Routes
# --------------------------------------------------------------------------- #


@app.route("/health", methods=["GET"])
def health_check() -> tuple:
    """Health check endpoint."""
    status = "healthy" if predictor and predictor.is_ready else "degraded"
    return jsonify({
        "status": status,
        "model_loaded": predictor.is_ready if predictor else False,
        "timestamp": datetime.utcnow().isoformat() + "Z",
    }), 200 if status == "healthy" else 503


@app.route("/predict", methods=["POST"])
def predict_single() -> tuple:
    """
    Single prediction.

    Request JSON::

        {
            "restaurantId": "rest_001",
            "date": "2024-03-15",
            "features": {
                "day_of_week": 4,
                "month": 3,
                "is_weekend": 0,
                "is_holiday": 0,
                "rolling_avg_7d": 12.5,
                "rolling_std_7d": 2.1,
                "lag_1d": 11.8,
                "lag_7d": 13.0,
                "category_encoded": 0,
                "weather_encoded": 1
            },
            "category": "PREPARED_MEALS",
            "historical_avg": 12.5
        }

    Alternatively supply ``historical_features`` (list of 7 dicts) instead
    of ``features`` for full sequence control.
    """
    if not predictor or not predictor.is_ready:
        return _error("Model not loaded. Train the model first.", 503)

    data = request.get_json(silent=True)
    if not data:
        return _error("Request body must be valid JSON.")

    restaurant_id = data.get("restaurantId", "unknown")
    date = data.get("date", "unknown")
    category = data.get("category", "UNKNOWN")
    historical_avg = float(data.get("historical_avg", 10.0))

    history = _build_history_from_request(data)

    # Validate at least the first entry
    if history:
        err = _validate_features(history[0])
        if err:
            return _error(err)

    try:
        result = predictor.predict(history)
    except Exception as exc:
        logger.exception("Prediction failed")
        return _error(f"Prediction error: {exc}", 500)

    # Recommendations
    recs = WastePredictor.generate_recommendations(
        predicted_kg=result["predicted_waste_kg"],
        category=category,
        day_of_week=history[-1].get("day_of_week", 0) if history else 0,
        historical_avg=historical_avg,
    )

    return jsonify({
        "restaurantId": restaurant_id,
        "date": date,
        "prediction": result,
        "recommendations": recs,
    }), 200


@app.route("/predict/batch", methods=["POST"])
def predict_batch() -> tuple:
    """
    Batch predictions.

    Request JSON::

        {
            "predictions": [
                {
                    "restaurantId": "rest_001",
                    "date": "2024-03-15",
                    "features": { ... },
                    "category": "BAKERY",
                    "historical_avg": 8.0
                },
                ...
            ]
        }
    """
    if not predictor or not predictor.is_ready:
        return _error("Model not loaded. Train the model first.", 503)

    data = request.get_json(silent=True)
    if not data or "predictions" not in data:
        return _error("Request must contain a 'predictions' array.")

    items = data["predictions"]
    if not items:
        return _error("'predictions' array is empty.")

    histories = []
    metadata = []
    for item in items:
        hist = _build_history_from_request(item)
        histories.append(hist)
        metadata.append({
            "restaurantId": item.get("restaurantId", "unknown"),
            "date": item.get("date", "unknown"),
            "category": item.get("category", "UNKNOWN"),
            "historical_avg": float(item.get("historical_avg", 10.0)),
        })

    try:
        results = predictor.predict_batch(histories)
    except Exception as exc:
        logger.exception("Batch prediction failed")
        return _error(f"Batch prediction error: {exc}", 500)

    response_items = []
    for res, meta in zip(results, metadata):
        recs = WastePredictor.generate_recommendations(
            predicted_kg=res["predicted_waste_kg"],
            category=meta["category"],
            day_of_week=0,
            historical_avg=meta["historical_avg"],
        )
        response_items.append({
            "restaurantId": meta["restaurantId"],
            "date": meta["date"],
            "prediction": res,
            "recommendations": recs,
        })

    return jsonify({
        "count": len(response_items),
        "predictions": response_items,
    }), 200


@app.route("/model/info", methods=["GET"])
def model_info() -> tuple:
    """Return model version, architecture info, and test metrics."""
    if not predictor:
        return _error("Predictor not initialised.", 503)
    return jsonify(predictor.get_model_info()), 200


@app.route("/model/retrain", methods=["POST"])
def retrain() -> tuple:
    """
    Trigger an asynchronous retraining run.

    Optional JSON body::

        {
            "data_path": "data/food_waste_data.csv",
            "epochs": 50
        }
    """
    global _retrain_status

    if _retrain_status["running"]:
        return jsonify({
            "status": "already_running",
            "started_at": _retrain_status["last_run"],
        }), 409

    data = request.get_json(silent=True) or {}
    data_path = data.get("data_path", "data/food_waste_data.csv")
    epochs = int(data.get("epochs", 100))

    def _retrain_worker():
        global _retrain_status
        try:
            _retrain_status["running"] = True
            _retrain_status["last_run"] = datetime.utcnow().isoformat() + "Z"

            from train import train as run_training

            result = run_training(data_path=data_path, epochs=epochs)
            _retrain_status["result"] = result

            # Reload predictor with new weights
            _init_predictor()
            logger.info("Retraining complete. Model reloaded.")
        except Exception as exc:
            logger.exception("Retraining failed")
            _retrain_status["result"] = {"error": str(exc)}
        finally:
            _retrain_status["running"] = False

    with _retrain_lock:
        thread = threading.Thread(target=_retrain_worker, daemon=True)
        thread.start()

    return jsonify({
        "status": "retraining_started",
        "data_path": data_path,
        "epochs": epochs,
        "started_at": _retrain_status["last_run"],
    }), 202


@app.route("/model/retrain/status", methods=["GET"])
def retrain_status() -> tuple:
    """Check the status of an ongoing or last-completed retraining run."""
    return jsonify(_retrain_status), 200


# --------------------------------------------------------------------------- #
# Error handlers
# --------------------------------------------------------------------------- #


@app.errorhandler(404)
def not_found(_):
    return jsonify({"error": "Endpoint not found"}), 404


@app.errorhandler(405)
def method_not_allowed(_):
    return jsonify({"error": "Method not allowed"}), 405


@app.errorhandler(500)
def internal_error(_):
    return jsonify({"error": "Internal server error"}), 500


# --------------------------------------------------------------------------- #
# Entrypoint
# --------------------------------------------------------------------------- #

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    debug = os.environ.get("FLASK_DEBUG", "0") == "1"
    logger.info("Starting Food Waste Prediction API on port %d", port)
    app.run(host="0.0.0.0", port=port, debug=debug)
