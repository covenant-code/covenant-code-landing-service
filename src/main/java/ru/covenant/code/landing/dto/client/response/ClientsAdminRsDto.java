package ru.covenant.code.landing.dto.client.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Schema(description = "Данные клиента для административной панели")
public class ClientsAdminRsDto {

    @Schema(
            description = "Уникальный идентификатор клиента",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID id;

    @Schema(
            description = "Имя клиента",
            example = "Иван"
    )
    private String name;

    @Schema(
            description = "Email клиента",
            example = "ivan@example.com"
    )
    private String email;

    @Schema(
            description = "Телефон клиента",
            example = "+79161234567"
    )
    private String phone;

    @Schema(
            description = "Сообщение/комментарий клиента",
            example = "Хотел бы узнать подробнее о курсе"
    )
    private String message;

    @Schema(
            description = "Тип выбранного курса",
            example = "BACKEND"
    )
    private CourseType courseType;

    @Schema(
            description = "Статус заявки",
            example = "NEW"
    )
    private Status status;

    @Schema(
            description = "Приоритет заявки",
            example = "MEDIUM"
    )
    private Priority priority;

    @Schema(
            description = "Название статуса на русском",
            example = "Новые"
    )
    private String statusLabel;

    @Schema(
            description = "Название приоритета на русском",
            example = "Средний"
    )
    private String priorityLabel;

    @Schema(
            description = "Источник заявки",
            example = "Лендинг"
    )
    private String source;

    @Schema(
            description = "Дата создания (в формате строки)",
            example = "2024-01-15T14:30:00"
    )
    private String createdAt;

    @Schema(
            description = "Дата обновления (в формате строки)",
            example = "2024-01-16T10:15:00"
    )
    private String updatedAt;

    @Schema(
            description = "Дата обработки (в формате строки)",
            example = "2024-01-16T10:15:00"
    )
    private String processedAt;

    @Schema(
            description = "Администратор, обработавший заявку",
            example = "admin@covenantcode.ru"
    )
    private String processedBy;

    @Schema(
            description = "Отформатированная дата создания для отображения",
            example = "15.01.2024 14:30"
    )
    private String formattedCreatedAt;

    @Schema(
            description = "Отформатированная дата обновления для отображения",
            example = "16.01.2024 10:15"
    )
    private String formattedUpdatedAt;

    @Schema(
            description = "Отформатированная дата обработки для отображения",
            example = "16.01.2024 10:15"
    )
    private String formattedProcessedAt;
}