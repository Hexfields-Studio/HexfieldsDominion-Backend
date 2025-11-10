# ======== 1. Build stage ========
FROM eclipse-temurin:21-jdk AS builder

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

RUN ./gradlew clean build -x test

# ======== 2. Runtime stage ========
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port (match your application.properties server.port)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]