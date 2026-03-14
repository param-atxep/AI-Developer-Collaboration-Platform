#!/usr/bin/env bash
# =============================================================================
# Deployment Script - AI Food Waste Redistribution Platform
#
# Builds Docker images for all services, pushes them to the configured
# registry, applies Kubernetes manifests, waits for rollout, and runs
# health checks.
#
# Usage:
#   ./deploy.sh --env staging                     # deploy all services
#   ./deploy.sh --env production --services "auth-service,user-service"
#   ./deploy.sh --env staging --tag v1.2.3
#   ./deploy.sh --env production --dry-run
#
# Prerequisites: docker, aws cli, kubectl, jq
# =============================================================================

set -euo pipefail

# ---------------------------------------------------------------------------
# Colours / helpers
# ---------------------------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

info()    { echo -e "${BLUE}[INFO]${NC}  $*"; }
success() { echo -e "${GREEN}[OK]${NC}    $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*"; exit 1; }

# ---------------------------------------------------------------------------
# Defaults
# ---------------------------------------------------------------------------
ENVIRONMENT=""
SERVICES=""
IMAGE_TAG=""
DRY_RUN=false
SKIP_BUILD=false
SKIP_PUSH=false
SKIP_HEALTHCHECK=false
ROLLOUT_TIMEOUT="600s"

ALL_SERVICES=(
  api-gateway
  auth-service
  user-service
  food-listing-service
  geolocation-service
  pickup-service
  notification-service
  analytics-service
  ai-prediction-service
  frontend
)

# ---------------------------------------------------------------------------
# Parse arguments
# ---------------------------------------------------------------------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    --env)            ENVIRONMENT="$2"; shift 2 ;;
    --services)       SERVICES="$2"; shift 2 ;;
    --tag)            IMAGE_TAG="$2"; shift 2 ;;
    --dry-run)        DRY_RUN=true; shift ;;
    --skip-build)     SKIP_BUILD=true; shift ;;
    --skip-push)      SKIP_PUSH=true; shift ;;
    --skip-healthcheck) SKIP_HEALTHCHECK=true; shift ;;
    --timeout)        ROLLOUT_TIMEOUT="$2"; shift 2 ;;
    -h|--help)
      echo "Usage: $0 --env <staging|production> [options]"
      echo ""
      echo "Options:"
      echo "  --env ENV              Target environment (required)"
      echo "  --services LIST        Comma-separated service list (default: all)"
      echo "  --tag TAG              Docker image tag (default: git SHA)"
      echo "  --dry-run              Print commands without executing"
      echo "  --skip-build           Skip Docker image build step"
      echo "  --skip-push            Skip pushing images to registry"
      echo "  --skip-healthcheck     Skip post-deploy health checks"
      echo "  --timeout DURATION     Kubectl rollout timeout (default: 600s)"
      exit 0
      ;;
    *) error "Unknown argument: $1" ;;
  esac
done

# ---------------------------------------------------------------------------
# Validation
# ---------------------------------------------------------------------------
[ -z "$ENVIRONMENT" ] && error "Missing required argument: --env <staging|production>"

case "$ENVIRONMENT" in
  staging)    NAMESPACE="staging";    EKS_CLUSTER="food-waste-staging"    ;;
  production) NAMESPACE="production"; EKS_CLUSTER="food-waste-production" ;;
  *)          error "Invalid environment: ${ENVIRONMENT}. Use 'staging' or 'production'." ;;
esac

# Resolve project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# Resolve services list
if [ -n "$SERVICES" ]; then
  IFS=',' read -ra DEPLOY_SERVICES <<< "$SERVICES"
else
  DEPLOY_SERVICES=("${ALL_SERVICES[@]}")
fi

# Resolve image tag (default: short git SHA)
if [ -z "$IMAGE_TAG" ]; then
  IMAGE_TAG=$(git -C "$PROJECT_ROOT" rev-parse --short HEAD 2>/dev/null || echo "latest")
fi

# AWS config
AWS_REGION="${AWS_REGION:-us-east-1}"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text 2>/dev/null || echo "000000000000")
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Deployment Plan${NC}"
echo -e "${BLUE}========================================${NC}"
echo "  Environment : ${ENVIRONMENT}"
echo "  Namespace   : ${NAMESPACE}"
echo "  EKS Cluster : ${EKS_CLUSTER}"
echo "  ECR Registry: ${ECR_REGISTRY}"
echo "  Image Tag   : ${IMAGE_TAG}"
echo "  Services    : ${DEPLOY_SERVICES[*]}"
echo "  Dry Run     : ${DRY_RUN}"
echo -e "${BLUE}========================================${NC}"
echo ""

run_cmd() {
  if [ "$DRY_RUN" = true ]; then
    echo -e "${YELLOW}[DRY-RUN]${NC} $*"
  else
    "$@"
  fi
}

# ===========================================================================
# 1. Build Docker images
# ===========================================================================
if [ "$SKIP_BUILD" = true ]; then
  info "Skipping Docker image build (--skip-build)."
else
  info "Building Docker images..."

  for svc in "${DEPLOY_SERVICES[@]}"; do
    svc=$(echo "$svc" | xargs)

    if [ "$svc" = "frontend" ]; then
      CONTEXT="${PROJECT_ROOT}/frontend"
    else
      CONTEXT="${PROJECT_ROOT}/backend/${svc}"
    fi

    if [ ! -f "${CONTEXT}/Dockerfile" ]; then
      warn "Dockerfile not found for ${svc} at ${CONTEXT}/Dockerfile. Skipping build."
      continue
    fi

    IMAGE="${ECR_REGISTRY}/food-waste/${svc}"
    info "Building ${svc} -> ${IMAGE}:${IMAGE_TAG}"

    run_cmd docker build \
      -t "${IMAGE}:${IMAGE_TAG}" \
      -t "${IMAGE}:${ENVIRONMENT}-latest" \
      --build-arg BUILD_VERSION="${IMAGE_TAG}" \
      "${CONTEXT}"

    success "Built ${svc}."
  done
fi

# ===========================================================================
# 2. Push images to ECR
# ===========================================================================
if [ "$SKIP_PUSH" = true ]; then
  info "Skipping image push (--skip-push)."
else
  info "Logging into ECR..."
  run_cmd aws ecr get-login-password --region "${AWS_REGION}" \
    | docker login --username AWS --password-stdin "${ECR_REGISTRY}" 2>/dev/null

  info "Pushing Docker images..."

  for svc in "${DEPLOY_SERVICES[@]}"; do
    svc=$(echo "$svc" | xargs)
    IMAGE="${ECR_REGISTRY}/food-waste/${svc}"

    info "Pushing ${IMAGE}:${IMAGE_TAG}"
    run_cmd docker push "${IMAGE}:${IMAGE_TAG}"
    run_cmd docker push "${IMAGE}:${ENVIRONMENT}-latest"
    success "Pushed ${svc}."
  done
fi

# ===========================================================================
# 3. Configure kubectl and apply manifests
# ===========================================================================
info "Configuring kubectl for cluster ${EKS_CLUSTER}..."
run_cmd aws eks update-kubeconfig \
  --region "${AWS_REGION}" \
  --name "${EKS_CLUSTER}"

# Apply base manifests
K8S_BASE="${PROJECT_ROOT}/infrastructure/k8s/base"
K8S_OVERLAY="${PROJECT_ROOT}/infrastructure/k8s/overlays/${ENVIRONMENT}"

if [ -d "$K8S_BASE" ]; then
  info "Applying base Kubernetes manifests..."
  run_cmd kubectl apply -f "$K8S_BASE" -n "${NAMESPACE}"
fi

if [ -d "$K8S_OVERLAY" ]; then
  info "Applying ${ENVIRONMENT} overlay manifests..."
  run_cmd kubectl apply -f "$K8S_OVERLAY" -n "${NAMESPACE}"
fi

# Update deployment images
info "Updating deployment images..."
for svc in "${DEPLOY_SERVICES[@]}"; do
  svc=$(echo "$svc" | xargs)
  IMAGE="${ECR_REGISTRY}/food-waste/${svc}:${IMAGE_TAG}"

  info "  ${svc} -> ${IMAGE}"
  run_cmd kubectl set image "deployment/${svc}" \
    "${svc}=${IMAGE}" \
    -n "${NAMESPACE}"
done

# ===========================================================================
# 4. Wait for rollout
# ===========================================================================
info "Waiting for deployments to roll out (timeout: ${ROLLOUT_TIMEOUT})..."

FAILED_SERVICES=()
for svc in "${DEPLOY_SERVICES[@]}"; do
  svc=$(echo "$svc" | xargs)
  info "  Waiting for ${svc}..."
  if ! run_cmd kubectl rollout status "deployment/${svc}" \
       -n "${NAMESPACE}" --timeout="${ROLLOUT_TIMEOUT}"; then
    warn "  ${svc} rollout FAILED."
    FAILED_SERVICES+=("$svc")
  else
    success "  ${svc} is ready."
  fi
done

if [ ${#FAILED_SERVICES[@]} -gt 0 ]; then
  warn "The following services failed to roll out: ${FAILED_SERVICES[*]}"
  warn "Initiating rollback for failed services..."

  for svc in "${FAILED_SERVICES[@]}"; do
    info "  Rolling back ${svc}..."
    run_cmd kubectl rollout undo "deployment/${svc}" -n "${NAMESPACE}"
  done

  # Wait for rollback
  for svc in "${FAILED_SERVICES[@]}"; do
    run_cmd kubectl rollout status "deployment/${svc}" \
      -n "${NAMESPACE}" --timeout="300s" || true
  done

  error "Deployment failed. Rolled back: ${FAILED_SERVICES[*]}"
fi

# ===========================================================================
# 5. Health checks
# ===========================================================================
if [ "$SKIP_HEALTHCHECK" = true ]; then
  info "Skipping health checks (--skip-healthcheck)."
else
  info "Running post-deployment health checks..."

  case "$ENVIRONMENT" in
    staging)    BASE_URL="https://staging.foodwaste.example.com" ;;
    production) BASE_URL="https://foodwaste.example.com" ;;
  esac

  health_check() {
    local name="$1"
    local url="$2"
    local max_retries=5
    local retry=0

    while [ $retry -lt $max_retries ]; do
      HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "$url" 2>/dev/null || echo "000")
      if [ "$HTTP_CODE" = "200" ]; then
        success "  ${name}: HTTP ${HTTP_CODE}"
        return 0
      fi
      ((retry++))
      warn "  ${name}: HTTP ${HTTP_CODE} (attempt ${retry}/${max_retries})"
      sleep 5
    done

    error "  ${name}: Health check failed after ${max_retries} attempts."
  }

  health_check "API Gateway"       "${BASE_URL}/actuator/health"
  health_check "Auth Service"      "${BASE_URL}/api/v1/auth/health"
  health_check "Food Listings"     "${BASE_URL}/api/v1/food-listings/health"
  health_check "Pickup Service"    "${BASE_URL}/api/v1/pickups/health"

  success "All health checks passed."
fi

# ===========================================================================
# Done
# ===========================================================================
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Deployment complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo "  Environment : ${ENVIRONMENT}"
echo "  Image Tag   : ${IMAGE_TAG}"
echo "  Services    : ${DEPLOY_SERVICES[*]}"
echo -e "${GREEN}========================================${NC}"
echo ""
