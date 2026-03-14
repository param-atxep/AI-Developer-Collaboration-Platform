# =============================================================================
# Stage 1: Build
# =============================================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy dependency descriptor first for better layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B && \
    mv target/*.jar target/app.jar

# =============================================================================
# Stage 2: Runtime
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Install curl for healthcheck
RUN apk add --no-cache curl tini

WORKDIR /app

# Copy the built artifact from the build stage
COPY --from=build /app/target/app.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose service port
EXPOSE 8087

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0 \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.output.ansi.enabled=ALWAYS"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8087/actuator/health || exit 1

# Use tini as init system for proper signal handling
ENTRYPOINT ["tini", "--"]
CMD ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
