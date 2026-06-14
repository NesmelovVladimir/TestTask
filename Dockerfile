# ---- этап сборки ----
FROM gradle:9.5.1-jdk25-alpine AS build
WORKDIR /app

# Кешируем зависимости
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon || true

# Копируем исходники и собираем bootJar
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# ---- этап запуска ----
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]