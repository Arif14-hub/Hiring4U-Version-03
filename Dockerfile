# =========================
# Stage 1: Build Spring Boot
# =========================
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy Maven configuration
COPY pom.xml .

# Download dependencies first for Docker layer caching
RUN mvn dependency:go-offline -B

# Copy application source
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests


# =========================
# Stage 2: Run application
# =========================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy Spring Boot JAR
COPY --from=builder /app/target/*.jar app.jar

# Spring Boot port
EXPOSE 8080

# Use your local H2 configuration
ENV SPRING_PROFILES_ACTIVE=local

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]