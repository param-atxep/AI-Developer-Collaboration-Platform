# ---------------------------------------------------------------------------
# Dockerfile -- Food Waste Prediction AI Model Service
# ---------------------------------------------------------------------------
# Multi-stage is overkill here since we're not compiling anything, but we
# keep the image lean with python:3.11-slim and careful layer ordering.
# ---------------------------------------------------------------------------

FROM python:3.11-slim AS base

# Prevent Python from buffering stdout/stderr (important for logging in Docker)
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PIP_NO_CACHE_DIR=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1

# Install OS-level deps that TensorFlow / numpy may need at runtime
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        libhdf5-dev \
        libgl1 \
        curl && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r mluser && useradd -r -g mluser -d /app -s /sbin/nologin mluser

WORKDIR /app

# ---------------------------------------------------------------------------
# Dependencies (cached layer -- only re-runs when requirements.txt changes)
# ---------------------------------------------------------------------------
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# ---------------------------------------------------------------------------
# Application code
# ---------------------------------------------------------------------------
COPY src/ src/
COPY models/ models/
COPY data/ data/

# Create directories that the app expects at runtime
RUN mkdir -p models/saved_model models/scalers models/checkpoints logs data && \
    chown -R mluser:mluser /app

USER mluser

# ---------------------------------------------------------------------------
# Expose port and healthcheck
# ---------------------------------------------------------------------------
EXPOSE 5000

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:5000/health || exit 1

# ---------------------------------------------------------------------------
# Default entrypoint: gunicorn serving the Flask API
# ---------------------------------------------------------------------------
ENV MODEL_DIR=models/saved_model \
    SCALER_DIR=models/scalers \
    METRICS_PATH=models/training_metrics.json \
    PORT=5000

CMD ["gunicorn", \
     "--bind", "0.0.0.0:5000", \
     "--workers", "2", \
     "--threads", "4", \
     "--timeout", "120", \
     "--access-logfile", "-", \
     "--error-logfile", "-", \
     "src.api_server:app"]
