package ru.covenant.code.landing.dto.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Ответ на аутентификацию администратора")
public class AdminLoginRsDto {

    @Schema(
            description = "Успешность операции",
            example = "true"
    )
    private boolean success;

    @Schema(
            description = "Сообщение об ошибке или успехе",
            example = "Успешная аутентификация"
    )
    private String message;

    @Schema(
            description = "Флаг аутентификации",
            example = "true"
    )
    private boolean authenticated;

    @Schema(
            description = "Email аутентифицированного пользователя",
            example = "admin@covenantcode.ru"
    )
    private String email;

    @Schema(
            description = "Роль аутентифицированного пользователя",
            example = "ADMIN",
            allowableValues = {"SUPER_ADMIN", "ADMIN", "MODERATOR", "SUPPORT"}
    )
    private String role;
}