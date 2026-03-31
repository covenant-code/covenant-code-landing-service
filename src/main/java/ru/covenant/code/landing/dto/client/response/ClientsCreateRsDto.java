package ru.covenant.code.landing.dto.client.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ на создание заявки клиента")
public class ClientsCreateRsDto {

    @Schema(description = "Успешность операции", example = "true")
    private boolean success;

    @Schema(description = "Результат операции")
    private Result result;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Информация о созданной заявке")
    public static class Result {
        @Schema(description = "Идентификатор созданной заявки", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID id;

        @Schema(description = "Имя клиента", example = "Иван Иванов")
        private String name;

        @Schema(description = "Email клиента", example = "ivan@example.com")
        private String email;

        @Schema(description = "Телефон клиента", example = "+79161234567")
        private String phone;

        @Schema(description = "Тип курса", example = "BACKEND")
        private String courseType;

        @Schema(description = "Дата и время создания заявки", example = "2024-01-01T12:00:00Z")
        private LocalDateTime createdAt;

        @Schema(description = "Статус создания заявки", example = "SUCCESS", defaultValue = "SUCCESS")
        private String status;
    }

    @Schema(description = "Сообщение о результате создания заявки",
            example = "Заявка успешно создана",
            defaultValue = "Заявка успешно создана")
    private String message;
}
