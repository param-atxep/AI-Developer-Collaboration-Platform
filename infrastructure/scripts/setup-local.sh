#!/usr/bin/env bash
# =============================================================================
# Local Development Setup - AI Food Waste Redistribution Platform
#
# Usage:  ./setup-local.sh [--skip-infra] [--skip-migrations]
#
# Prerequisites: java 17+, node 20+, docker, docker-compose, kubectl (optional)
# =============================================================================

set -euo pipefail

# ---------------------------------------------------------------------------
# Colours / helpers
# ---------------------------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Colour

info()    { echo -e "${BLUE}[INFO]${NC}  $*"; }
success() { echo -e "${GREEN}[OK]${NC}    $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*"; exit 1; }

# ---------------------------------------------------------------------------
# Flags
# ---------------------------------------------------------------------------
SKIP_INFRA=false
SKIP_MIGRATIONS=false

for arg in "$@"; do
  case "$arg" in
    --skip-infra)       SKIP_INFRA=true ;;
    --skip-migrations)  SKIP_MIGRATIONS=true ;;
    -h|--help)
      echo "Usage: $0 [--skip-infra] [--skip-migrations]"
      exit 0
      ;;
  esac
done

# ---------------------------------------------------------------------------
# Project root (two levels up from this script)
# ---------------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

info "Project root: ${PROJECT_ROOT}"

# ===========================================================================
# 1. Check prerequisites
# ===========================================================================
info "Checking prerequisites..."

check_command() {
  local cmd="$1"
  local min_version="${2:-}"
  if ! command -v "$cmd" &>/dev/null; then
    error "'$cmd' is not installed. Please install it and retry."
  fi
  local ver
  ver=$("$cmd" --version 2>&1 | head -1) || true
  success "$cmd found: $ver"
}

check_command java
check_command mvn
check_command node
check_command npm
check_command docker
check_command docker-compose  # also accepts "docker compose" via alias

# Java version check (need 17+)
JAVA_VER=$(java -version 2>&1 | head -1 | sed -E 's/.*"([0-9]+).*/\1/')
if [ "$JAVA_VER" -lt 17 ]; then
  error "Java 17+ is required (found ${JAVA_VER})."
fi
success "Java version ${JAVA_VER} meets requirement (>=17)."

# Node version check (need 20+)
NODE_VER=$(node -v | sed 's/v//' | cut -d. -f1)
if [ "$NODE_VER" -lt 20 ]; then
  error "Node.js 20+ is required (found ${NODE_VER})."
fi
success "Node.js version $(node -v) meets requirement (>=20)."

# kubectl is optional
if command -v kubectl &>/dev/null; then
  success "kubectl found: $(kubectl version --client --short 2>/dev/null || kubectl version --client 2>&1 | head -1)"
else
  warn "kubectl not found - not needed for local dev, but required for K8s deployment."
fi

# ===========================================================================
# 2. Start infrastructure via Docker Compose
# ===========================================================================
if [ "$SKIP_INFRA" = true ]; then
  info "Skipping infrastructure startup (--skip-infra)."
else
  info "Starting infrastructure services (PostgreSQL, Redis, Kafka)..."

  COMPOSE_FILE="${PROJECT_ROOT}/infrastructure/docker-compose.yml"

  if [ ! -f "$COMPOSE_FILE" ]; then
    warn "docker-compose.yml not found at ${COMPOSE_FILE}. Creating minimal compose file..."
    mkdir -p "$(dirname "$COMPOSE_FILE")"
    cat > "$COMPOSE_FILE" <<'COMPOSE_EOF'
version: "3.9"

services:
  postgres:
    image: postgres:16-alpine
    container_name: foodwaste-postgres
    environment:
      POSTGRES_DB: foodwaste
      POSTGRES_USER: foodwaste
      POSTGRES_PASSWORD: foodwaste_local
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U foodwaste -d foodwaste"]
      interval: 5s
      timeout: 5s
      retries: 10

  redis:
    image: redis:7-alpine
    container_name: foodwaste-redis
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 5s
      retries: 10

  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    container_name: foodwaste-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    healthcheck:
      test: ["CMD-SHELL", "echo ruok | nc localhost 2181 | grep imok"]
      interval: 10s
      timeout: 5s
      retries: 10

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    container_name: foodwaste-kafka
    depends_on:
      zookeeper:
        condition: service_healthy
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    ports:
      - "9092:9092"
    healthcheck:
      test: ["CMD-SHELL", "kafka-broker-api-versions --bootstrap-server localhost:9092"]
      interval: 10s
      timeout: 10s
      retries: 15

volumes:
  pgdata:
COMPOSE_EOF
    success "Created docker-compose.yml."
  fi

  docker-compose -f "$COMPOSE_FILE" up -d

  # -------------------------------------------------------------------------
  # Wait for services to be healthy
  # -------------------------------------------------------------------------
  info "Waiting for infrastructure services to become healthy..."

  wait_for_service() {
    local name="$1"
    local max_attempts="${2:-30}"
    local attempt=1
    while [ $attempt -le "$max_attempts" ]; do
      STATUS=$(docker inspect --format='{{.State.Health.Status}}' "$name" 2>/dev/null || echo "missing")
      if [ "$STATUS" = "healthy" ]; then
        success "$name is healthy."
        return 0
      fi
      echo -n "."
      sleep 2
      ((attempt++))
    done
    error "$name did not become healthy within $((max_attempts * 2))s."
  }

  wait_for_service "foodwaste-postgres"
  wait_for_service "foodwaste-redis"
  wait_for_service "foodwaste-kafka" 45
fi

# ===========================================================================
# 3. Run database migrations
# ===========================================================================
if [ "$SKIP_MIGRATIONS" = true ]; then
  info "Skipping database migrations (--skip-migrations)."
else
  info "Running database migrations..."

  MIGRATION_DIR="${PROJECT_ROOT}/database/migrations"
  if [ -d "$MIGRATION_DIR" ]; then
    # Prefer Flyway Maven plugin if the backend builds include it
    if [ -f "${PROJECT_ROOT}/backend/pom.xml" ]; then
      (
        cd "${PROJECT_ROOT}/backend"
        mvn flyway:migrate \
          -Dflyway.url="jdbc:postgresql://localhost:5432/foodwaste" \
          -Dflyway.user="foodwaste" \
          -Dflyway.password="foodwaste_local" \
          -Dflyway.locations="filesystem:${MIGRATION_DIR}" \
          -pl auth-service -q 2>/dev/null
      ) && success "Flyway migrations applied." || warn "Flyway migration skipped (plugin may not be configured)."
    fi

    # Fallback: apply raw SQL files
    for sql_file in "${MIGRATION_DIR}"/*.sql; do
      [ -f "$sql_file" ] || continue
      info "  Applying $(basename "$sql_file")..."
      docker exec -i foodwaste-postgres \
        psql -U foodwaste -d foodwaste < "$sql_file" >/dev/null 2>&1 || true
    done
    success "Database migrations complete."
  else
    warn "No migration directory found at ${MIGRATION_DIR}. Skipping."
  fi
fi

# ===========================================================================
# 4. Install frontend dependencies
# ===========================================================================
if [ -f "${PROJECT_ROOT}/frontend/package.json" ]; then
  info "Installing frontend dependencies..."
  (cd "${PROJECT_ROOT}/frontend" && npm ci --silent)
  success "Frontend dependencies installed."
else
  warn "No frontend/package.json found. Skipping npm install."
fi

# ===========================================================================
# 5. Print status
# ===========================================================================
echo ""
echo -e "${GREEN}=============================================${NC}"
echo -e "${GREEN}  Local environment is ready!${NC}"
echo -e "${GREEN}=============================================${NC}"
echo ""
echo "  Infrastructure services:"
echo "    PostgreSQL : localhost:5432  (user: foodwaste / pass: foodwaste_local)"
echo "    Redis      : localhost:6379"
echo "    Kafka      : localhost:9092"
echo "    Zookeeper  : localhost:2181"
echo ""
echo "  To start a backend service (e.g. auth-service):"
echo "    cd backend/auth-service"
echo "    mvn spring-boot:run -Dspring-boot.run.profiles=local"
echo ""
echo "  To start the frontend:"
echo "    cd frontend"
echo "    npm run dev"
echo ""
echo "  To stop infrastructure:"
echo "    docker-compose -f infrastructure/docker-compose.yml down"
echo ""
