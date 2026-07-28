FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY gradle/ gradle/
COPY gradlew gradlew
RUN chmod +x gradlew  
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts
RUN ./gradlew dependencies --no-daemon || true

COPY src src
RUN ./gradlew bootJar -x test --no-daemon



FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
