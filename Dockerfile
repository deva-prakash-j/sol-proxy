# Multi-stage build for optimized Spring Boot application
# Build stage  
FROM gradle:8.14-jdk21 AS builder

# Set working directory
WORKDIR /app

# Copy gradle files first for better layer caching
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

# Temporarily modify build.gradle to use Java 21 for Docker build
RUN sed -i 's/languageVersion = JavaLanguageVersion.of(24)/languageVersion = JavaLanguageVersion.of(21)/' build.gradle

# Download dependencies (this layer will be cached if dependencies don't change)
RUN gradle dependencies --no-daemon

# Copy source code
COPY src src

# Build the application  
RUN gradle bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre

# Add labels for better maintainability
LABEL description="Sol Proxy Spring Boot Application"
LABEL version="0.0.1-SNAPSHOT"

# Create a non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Set working directory
WORKDIR /app

# Install curl for health checks (optional)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose the port your app runs on
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization arguments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UnlockExperimentalVMOptions -XX:+UseZGC"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
