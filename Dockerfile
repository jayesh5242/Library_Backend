# Stage 1: Build the application
FROM maven:3.9.5-amazoncorretto-21 AS builder

# Set working directory inside container
WORKDIR /app

# Copy pom.xml first (for dependency caching)
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the JAR file (skip tests for faster build)
RUN mvn clean package -DskipTests

# ─────────────────────────────────────────────────────
# Stage 2: Run the application (smaller final image)
FROM amazoncorretto:21-alpine

WORKDIR /app

# Copy only the JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 9090

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]