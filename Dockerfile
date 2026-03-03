# Этап 1: Сборка
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2: Запуск
FROM amazoncorretto:21

WORKDIR /app

# Копируем JAR из первого этапа (builder)
COPY --from=builder /app/target/*.jar app.jar

# Создаем пользователя для безопасности
# RUN addgroup -S spring && adduser -S spring -G spring
# USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]