# AI Food Waste Prediction Model

LSTM-based TensorFlow model that predicts daily food waste (kg) per restaurant and food category, enabling proactive redistribution to food banks and shelters.

## Project Structure

```
ai-model/
  data/                        # Training data (CSV)
  models/                      # Saved model, scalers, checkpoints
  logs/                        # Training logs and curves
  notebooks/                   # (reserved for Jupyter analysis)
  src/
    generate_sample_data.py    # Synthetic data generator
    data_preprocessing.py      # Cleaning, feature engineering, normalization
    model.py                   # LSTM model definition (Keras)
    train.py                   # End-to-end training script
    predict.py                 # Inference module with confidence intervals
    api_server.py              # Flask REST API
  Dockerfile                   # Container image
  requirements.txt             # Python dependencies
```

## Quick Start

### 1. Install dependencies

```bash
cd ai-model
python -m venv .venv
source .venv/bin/activate        # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

### 2. Generate synthetic training data

```bash
cd src
python generate_sample_data.py --output ../data/food_waste_data.csv
```

This creates ~18,250 rows covering 10 restaurants, 365 days, and 5 food categories with realistic seasonal, weekly, and holiday patterns.

### 3. Train the model

```bash
python train.py --data ../data/food_waste_data.csv --epochs 50
```

Outputs:
- `models/saved_model/` -- TensorFlow SavedModel
- `models/scalers/` -- MinMaxScaler pickles
- `models/training_metrics.json` -- MAE, RMSE, R-squared
- `logs/training_curves.png` -- loss and MAE plots

### 4. Run the API server

```bash
python api_server.py
```

The server starts on `http://localhost:5000`.

### 5. Make a prediction

```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "rest_001",
    "date": "2024-03-15",
    "category": "PREPARED_MEALS",
    "historical_avg": 12.5,
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
    }
  }'
```

## API Endpoints

| Method | Path                   | Description                        |
|--------|------------------------|------------------------------------|
| GET    | `/health`              | Health check                       |
| POST   | `/predict`             | Single-day waste prediction        |
| POST   | `/predict/batch`       | Batch predictions (multiple items) |
| GET    | `/model/info`          | Model version and test metrics     |
| POST   | `/model/retrain`       | Trigger async retraining           |
| GET    | `/model/retrain/status`| Check retraining progress          |

## Docker

```bash
# Build
docker build -t food-waste-ai .

# Run
docker run -p 5000:5000 food-waste-ai
```

## Model Architecture

```
Input (7 timesteps x 10 features)
  -> LSTM(64, return_sequences=True)
  -> Dropout(0.2)
  -> LSTM(32)
  -> Dropout(0.2)
  -> Dense(16, relu)
  -> Dense(1, linear)
```

- **Optimizer:** Adam (lr=0.001, with ReduceLROnPlateau)
- **Loss:** Mean Squared Error
- **Metric:** Mean Absolute Error
- **Early Stopping:** patience=10 on val_loss

## Features

| Feature           | Description                                      |
|-------------------|--------------------------------------------------|
| day_of_week       | 0 (Mon) to 6 (Sun)                              |
| month             | 1-12                                             |
| is_weekend        | Binary flag                                      |
| is_holiday        | Binary flag (within 1 day of US holidays)        |
| rolling_avg_7d    | 7-day rolling mean of waste_kg                   |
| rolling_std_7d    | 7-day rolling std of waste_kg                    |
| lag_1d            | Previous day waste_kg                            |
| lag_7d            | Same day last week waste_kg                      |
| category_encoded  | Label-encoded food category                      |
| weather_encoded   | Label-encoded weather condition                  |

## License

Internal use -- AI Food Waste Redistribution Platform.
