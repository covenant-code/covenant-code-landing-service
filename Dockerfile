FROM amazoncorretto:21-alpine

WORKDIR /app

COPY target/*.jar app.jar

# Создаем пользователя для безопасности
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]