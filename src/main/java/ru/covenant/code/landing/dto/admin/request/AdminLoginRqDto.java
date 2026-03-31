package ru.covenant.code.landing.dto.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.covenant.code.landing.validation.constraints.ValidEmail;
import ru.covenant.code.landing.validation.constraints.ValidPassword;

@Data
@Schema(description = "Запрос на аутентификацию администратора")
public class AdminLoginRqDto {

    @Schema(
            description = "Email администратора",
            example = "admin@covenantcode.ru",
            required = true
    )
    @NotBlank
    @ValidEmail(message = "Неверный формат email")
    private String email;

    @Schema(
            description = "Пароль администратора",
            example = "admin123",
            required = true
    )
    @NotBlank(message = "Пароль обязателен для заполнения")
    @ValidPassword
    private String password;
}
