package ru.covenant.code.landing.dto.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.covenant.code.landing.validation.constraints.*;

@Data
@Schema(description = "Запрос на аутентификацию администратора")
public class AdminLoginRqDto {

    @NotBlank(message = "Email обязателен")
    @ValidEmail(message = "Некорректный формат email")
    @Schema(
            description = "Email администратора",
            example = "admin@covenantcode.ru",
            required = true
    )
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Schema(
            description = "Пароль администратора",
            example = "SecurePass123",
            required = true
    )
    private String password;
}