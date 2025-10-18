FROM gradle:8.5-jdk21 AS builder
WORKDIR /usr/src/app
COPY src ./src
COPY build.gradle .
COPY settings.gradle .
RUN ["gradle", "bootJar"]

EXPOSE 8080

FROM eclipse-temurin:21-jdk
ARG JAR_FILE=build/libs/api-gateway-*.jar
COPY --from=builder /usr/src/app/${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
