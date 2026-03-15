# AI Food Waste Redistribution Platform — System Architecture

## 1. System Architecture Overview

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                              CLIENTS                                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │ Restaurant   │  │ NGO Web     │  │ Citizen     │  │ Admin       │        │
│  │ Dashboard    │  │ Portal      │  │ Mobile/Web  │  │ Panel       │        │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘        │
└─────────┼────────────────┼────────────────┼────────────────┼────────────────┘
          │                │                │                │
          └────────────────┴────────┬───────┴────────────────┘
                                    │ HTTPS / WSS
                            ┌───────▼───────┐
                            │  CloudFlare   │
                            │  CDN / WAF    │
                            └───────┬───────┘
                                    │
                            ┌───────▼───────┐
                            │  AWS ALB /    │
                            │  Load Balancer│
                            └───────┬───────┘
                                    │
                    ┌───────────────▼───────────────┐
                    │       API GATEWAY            │
                    │   (Spring Cloud Gateway)     │
                    │  • Rate Limiting             │
                    │  • JWT Validation            │
                    │  • Request Routing           │
                    │  • Circuit Breaking          │
                    └───────────────┬───────────────┘
                                    │
              ┌────────────────────▼────────────────────┐
              │         SERVICE MESH / DISCOVERY         │
              │        (Spring Cloud Eureka)             │
              └────┬────┬────┬────┬────┬────┬────┬──────┘
                   │    │    │    │    │    │    │
    ┌──────────────┼────┼────┼────┼────┼────┼────┼──────────────────┐
    │              │    │    │    │    │    │    │   MICROSERVICES   │
    │  ┌───────┐ ┌┴──┐┌┴──┐┌┴──┐┌┴──┐┌┴──┐┌┴──┐┌┴──────┐         │
    │  │Auth   │ │User││Food││Geo ││Pick││Noti││Anal││AI/ML  │         │
    │  │Service│ │Svc ││List││Loc ││up  ││fy  ││ytics││Predict│         │
    │  │       │ │    ││Svc ││Svc ││Svc ││Svc ││Svc ││Svc    │         │
    │  └───┬───┘ └─┬──┘└─┬──┘└─┬──┘└─┬──┘└─┬──┘└─┬──┘└──┬────┘         │
    └──────┼───────┼─────┼─────┼─────┼─────┼─────┼──────┼──────────────┘
           │       │     │     │     │     │     │      │
    ┌──────▼───────▼─────▼─────▼─────▼─────▼─────▼──────▼──────────────┐
    │                    EVENT BUS (Apache Kafka)                       │
    │  Topics: food.listed | food.claimed | pickup.scheduled |         │
    │          notification.send | waste.prediction | user.activity    │
    └──────────────────────────┬───────────────────────────────────────┘
                               │
    ┌──────────────────────────▼───────────────────────────────────────┐
    │                      DATA LAYER                                  │
    │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
    │  │ PostgreSQL   │  │    Redis     │  │ Elasticsearch│           │
    │  │ (Primary DB) │  │  (Cache +   │  │ (Search +    │           │
    │  │              │  │   Sessions)  │  │  Logging)    │           │
    │  └──────────────┘  └──────────────┘  └──────────────┘           │
    └──────────────────────────────────────────────────────────────────┘
```

## 2. Microservices Breakdown

| Service              | Port  | Responsibility                                     |
|----------------------|-------|----------------------------------------------------|
| api-gateway          | 8080  | Route, rate-limit, authenticate all inbound traffic |
| discovery-service    | 8761  | Eureka service registry                             |
| auth-service         | 8081  | JWT auth, login, registration, RBAC                 |
| user-service         | 8082  | User profiles, preferences, roles                   |
| food-listing-service | 8083  | CRUD for food listings, availability tracking       |
| geolocation-service  | 8084  | Geo-matching, distance calc, Google Maps integration |
| pickup-service       | 8085  | Pickup scheduling, status tracking, coordination     |
| notification-service | 8086  | Real-time alerts via WebSocket, push, email          |
| analytics-service    | 8087  | Dashboards, food-saved metrics, reporting            |
| ai-prediction-service| 8088  | TensorFlow waste prediction, trend analysis          |

## 3. Event-Driven Communication Flow

```
Restaurant uploads food ──► food-listing-service
       │
       ├──► Kafka: food.listed
       │         │
       │         ├──► geolocation-service (find nearby NGOs/citizens)
       │         │         │
       │         │         └──► Kafka: notification.geo-matched
       │         │                   │
       │         │                   └──► notification-service (push alert)
       │         │
       │         └──► ai-prediction-service (update waste model)
       │
       └──► analytics-service (update food-saved counters)

NGO/Citizen claims food ──► pickup-service
       │
       ├──► Kafka: food.claimed
       │         │
       │         └──► notification-service (notify restaurant)
       │
       └──► Kafka: pickup.scheduled
                  │
                  ├──► notification-service (confirm to both parties)
                  └──► analytics-service (track pickup)
```

## 4. Security Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    SECURITY LAYERS                       │
├─────────────────────────────────────────────────────────┤
│ Layer 1: Network     │ CloudFlare WAF, VPC, TLS 1.3    │
│ Layer 2: Gateway     │ Rate Limiting (1000 req/min)     │
│ Layer 3: Auth        │ JWT RS256, Refresh Tokens        │
│ Layer 4: RBAC        │ Role: ADMIN, RESTAURANT, NGO,    │
│                      │       CITIZEN, VOLUNTEER          │
│ Layer 5: Data        │ AES-256 encryption at rest       │
│ Layer 6: Transport   │ TLS/mTLS between services        │
│ Layer 7: Audit       │ Full audit logging (ELK Stack)   │
└─────────────────────────────────────────────────────────┘
```

## 5. Cloud Deployment Strategy (AWS)

```
┌─────────────────────────────────────────────┐
│              AWS Region (us-east-1)          │
│  ┌────────────────────────────────────────┐  │
│  │         EKS Kubernetes Cluster         │  │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐  │  │
│  │  │ Node    │ │ Node    │ │ Node    │  │  │
│  │  │ Pool 1  │ │ Pool 2  │ │ Pool 3  │  │  │
│  │  │ (app)   │ │ (app)   │ │ (data)  │  │  │
│  │  └─────────┘ └─────────┘ └─────────┘  │  │
│  └────────────────────────────────────────┘  │
│                                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐     │
│  │ RDS      │ │ ElastiCache│ │ MSK     │     │
│  │ Postgres │ │ Redis     │ │ Kafka   │     │
│  │ Multi-AZ │ │ Cluster   │ │ Cluster │     │
│  └──────────┘ └──────────┘ └──────────┘     │
│                                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐     │
│  │ S3       │ │ CloudWatch│ │ ECR     │     │
│  │ Storage  │ │ Monitoring│ │ Registry│     │
│  └──────────┘ └──────────┘ └──────────┘     │
└─────────────────────────────────────────────┘
```

## 6. Scalability Strategy

- **Horizontal Pod Autoscaler** on all microservices (CPU > 70% → scale up)
- **Kafka partitioning**: 12 partitions per topic for parallel consumption
- **Redis cluster**: 3 primary + 3 replica nodes for caching
- **PostgreSQL**: Read replicas for analytics, connection pooling via PgBouncer
- **CDN**: Static assets via CloudFront
- **Database sharding**: By region when user count exceeds 1M

## 7. Monitoring & Logging

| Tool            | Purpose                              |
|-----------------|--------------------------------------|
| Prometheus      | Metrics collection from all services |
| Grafana         | Dashboard visualization              |
| ELK Stack       | Centralized log aggregation          |
| Jaeger          | Distributed tracing                  |
| PagerDuty       | Incident alerting                    |
| Spring Actuator | Health checks and service metrics    |
