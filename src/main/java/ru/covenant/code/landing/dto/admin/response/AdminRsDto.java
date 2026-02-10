package ru.covenant.code.landing.dto.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.covenant.code.landing.entity.enumerated.AdminRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "DTO администратора")
public class AdminRsDto {

    @Schema(
            description = "Уникальный идентификатор администратора",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID id;

    @Schema(
            description = "Email администратора",
            example = "admin@covenantcode.ru"
    )
    private String email;

    @Schema(
            description = "Имя администратора",
            example = "Иван"
    )
    private String firstName;

    @Schema(
            description = "Фамилия администратора",
            example = "Иванов"
    )
    private String lastName;

    @Schema(
            description = "Телефон администратора",
            example = "+79161234567"
    )
    private String phone;

    @Schema(
            description = "Отдел/департамент",
            example = "Техническая поддержка"
    )
    private String department;

    @Schema(
            description = "Роль администратора",
            example = "ADMIN"
    )
    private AdminRole role;

    @Schema(
            description = "Статус активности",
            example = "true"
    )
    private boolean active;

    @Schema(
            description = "Дата и время последнего входа",
            example = "2024-01-15T14:30:00"
    )
    private LocalDateTime lastLoginAt;

    @Schema(
            description = "Дата и время создания",
            example = "2024-01-01T10:00:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Полное имя администратора",
            example = "Иван Иванов"
    )
    public String getFullName() {
        return firstName + " " + lastName;
    }
}