# Stage 1: Build the application
FROM gradle:8.12.1-jdk21 AS builder
WORKDIR /usr/src/app

# Copy only Gradle files first to leverage caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src ./src

# Ensure Gradle Wrapper has execute permissions
RUN chmod +x gradlew

# Force Gradle to use the wrapper version
RUN ./gradlew bootJar --no-daemon

# Stage 2: Run the application
FROM openjdk:21-jdk
WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /usr/src/app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
