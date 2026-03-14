# AI Food Waste Redistribution Platform

A production-grade platform that reduces restaurant food waste by connecting restaurants with NGOs and nearby citizens in real time using AI-driven predictions, geo-location matching, and event-driven architecture.

## System Architecture

```
Clients (React SPA)
       │
   CloudFlare CDN/WAF
       │
   AWS ALB / Load Balancer
       │
   API Gateway (Spring Cloud Gateway)
   ├── Rate Limiting (Redis)
   ├── JWT Validation
   └── Circuit Breaking (Resilience4j)
       │
   Service Discovery (Eureka)
       │
   ┌─────────────────────────────────────────────────┐
   │              Microservices                       │
   │  Auth · User · FoodListing · Geolocation        │
   │  Pickup · Notification · Analytics · AI/ML      │
   └─────────────────────────────────────────────────┘
       │
   Apache Kafka (Event Bus)
       │
   ┌───────────────────────────────────────┐
   │  PostgreSQL · Redis · Elasticsearch   │
   └───────────────────────────────────────┘
```

## Tech Stack

| Layer          | Technology                                  |
|----------------|---------------------------------------------|
| Frontend       | React 18, TypeScript, Tailwind CSS, Vite    |
| Backend        | Java 21, Spring Boot 3.2, Spring Cloud      |
| Database       | PostgreSQL 16 with PostGIS                  |
| Caching        | Redis 7                                     |
| Messaging      | Apache Kafka 3.6                            |
| AI/ML          | TensorFlow 2.15, Python 3.11               |
| Maps           | Google Maps API                             |
| Realtime       | WebSocket (STOMP)                           |
| Infrastructure | Docker, Kubernetes, Terraform, AWS          |
| Monitoring     | Prometheus, Grafana, ELK Stack, Jaeger      |
| CI/CD          | GitHub Actions                              |

## Microservices

| Service                | Port | Description                          |
|------------------------|------|--------------------------------------|
| discovery-service      | 8761 | Eureka service registry              |
| api-gateway            | 8080 | Central gateway, auth, rate limiting |
| auth-service           | 8081 | JWT authentication, RBAC             |
| user-service           | 8082 | User profiles, restaurant/NGO data   |
| food-listing-service   | 8083 | Food listing CRUD, search            |
| geolocation-service    | 8084 | Geo-matching, Google Maps            |
| pickup-service         | 8085 | Pickup scheduling & coordination     |
| notification-service   | 8086 | WebSocket, email, push alerts        |
| analytics-service      | 8087 | Dashboards, impact metrics           |
| ai-prediction-service  | 8088 | Waste prediction (TensorFlow)        |
| ai-model (Python)      | 5000 | TensorFlow model API                 |

## Quick Start

### Prerequisites

- Java 21+
- Node.js 20+
- Docker & Docker Compose
- Maven 3.9+

### 1. Clone and configure

```bash
cp .env.example .env
# Edit .env with your credentials
```

### 2. Start infrastructure

```bash
cd infrastructure/docker
docker-compose up -d postgres redis kafka zookeeper
```

### 3. Start backend services

```bash
# Start Discovery Service first
cd backend/discovery-service && mvn spring-boot:run

# Then start remaining services in separate terminals
cd backend/api-gateway && mvn spring-boot:run
cd backend/auth-service && mvn spring-boot:run
cd backend/user-service && mvn spring-boot:run
cd backend/food-listing-service && mvn spring-boot:run
cd backend/geolocation-service && mvn spring-boot:run
cd backend/pickup-service && mvn spring-boot:run
cd backend/notification-service && mvn spring-boot:run
cd backend/analytics-service && mvn spring-boot:run
cd backend/ai-prediction-service && mvn spring-boot:run
```

### 4. Start frontend

```bash
cd frontend
npm install
npm run dev
```

### 5. Full Docker deployment

```bash
cd infrastructure/docker
docker-compose up -d
```

Visit http://localhost:3000

## API Endpoints

### Authentication
```
POST /api/auth/register    - Register new user
POST /api/auth/login       - Login, get JWT tokens
POST /api/auth/refresh     - Refresh access token
POST /api/auth/validate    - Validate token
```

### Food Listings
```
POST   /api/food           - Create food listing
GET    /api/food/{id}      - Get listing by ID
GET    /api/food/nearby    - Search nearby (lat, lng, radius)
GET    /api/food/search    - Advanced search with filters
POST   /api/food/{id}/claim - Claim a listing
PUT    /api/food/{id}      - Update listing
DELETE /api/food/{id}      - Cancel listing
```

### Pickups
```
POST /api/pickups              - Schedule pickup
GET  /api/pickups/{id}         - Get pickup details
PUT  /api/pickups/{id}/status  - Update status
POST /api/pickups/{id}/complete - Complete pickup
POST /api/pickups/{id}/rate    - Rate pickup
```

### Analytics
```
GET /api/analytics/dashboard           - Platform overview
GET /api/analytics/restaurant/{id}     - Restaurant analytics
GET /api/analytics/food-saved          - Time series data
GET /api/analytics/leaderboard         - Top restaurants
GET /api/analytics/impact              - Environmental impact
```

### AI Predictions
```
POST /api/predictions/predict                  - Get waste prediction
GET  /api/predictions/restaurant/{id}/trend    - Waste trends
GET  /api/predictions/restaurant/{id}/recommendations - AI recommendations
```

## Project Structure

```
├── backend/
│   ├── discovery-service/          # Eureka Server
│   ├── api-gateway/                # Spring Cloud Gateway
│   ├── auth-service/               # Authentication & RBAC
│   ├── user-service/               # User profiles
│   ├── food-listing-service/       # Food CRUD & search
│   ├── geolocation-service/        # Geo-matching
│   ├── pickup-service/             # Pickup coordination
│   ├── notification-service/       # Real-time alerts
│   ├── analytics-service/          # Dashboards & metrics
│   └── ai-prediction-service/      # AI waste predictions
├── frontend/                       # React + Tailwind SPA
├── ai-model/                       # TensorFlow model & API
├── database/
│   ├── migrations/                 # SQL migrations
│   └── seeds/                      # Sample data
├── infrastructure/
│   ├── docker/                     # Docker Compose
│   ├── kubernetes/                 # K8s manifests
│   ├── terraform/                  # AWS infrastructure
│   └── scripts/                    # Setup & deploy scripts
├── monitoring/
│   ├── prometheus/                 # Metrics collection
│   ├── grafana/                    # Dashboards
│   ├── filebeat/                   # Log collection
│   └── logstash/                   # Log processing
├── .github/workflows/              # CI/CD pipelines
├── ARCHITECTURE.md                 # Architecture documentation
├── pom.xml                         # Root Maven POM
└── .env.example                    # Environment template
```

## Security

- JWT RS256 tokens with refresh token rotation
- Role-Based Access Control: ADMIN, RESTAURANT, NGO, CITIZEN, VOLUNTEER
- API Rate Limiting: 1000 req/min per user (Redis-backed)
- Data Encryption: AES-256 at rest, TLS 1.3 in transit
- Input Validation on all endpoints (Bean Validation)
- SQL Injection Protection via JPA parameterized queries

## Performance Targets

- 100,000+ concurrent users
- 10,000+ restaurants
- Real-time notifications < 1 second
- API response time p99 < 500ms
- 99.9% uptime SLA
#   A I - D e v e l o p e r - C o l l a b o r a t i o n - P l a t f o r m  
 