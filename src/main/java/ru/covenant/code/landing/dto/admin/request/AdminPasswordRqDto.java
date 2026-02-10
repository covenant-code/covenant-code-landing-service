package ru.covenant.code.landing.dto.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.covenant.code.landing.validation.constraints.*;

@Data
@PasswordMatch(
        passwordField = "newPassword",
        confirmField = "confirmPassword",
        message = "Новый пароль и подтверждение не совпадают"
)
@Schema(description = "Запрос на изменение пароля администратора")
public class AdminPasswordRqDto {

    @NotBlank(message = "Текущий пароль обязателен")
    @Schema(
            description = "Текущий пароль администратора",
            example = "OldPass123",
            required = true
    )
    private String currentPassword;

    @NotBlank(message = "Новый пароль обязателен")
    @ValidPassword(message = "Новый пароль должен содержать минимум 8 символов, включая цифры и буквы")
    @Schema(
            description = "Новый пароль администратора",
            example = "NewSecurePass456",
            required = true,
            minLength = 8
    )
    private String newPassword;

    @NotBlank(message = "Подтверждение пароля обязательно")
    @Schema(
            description = "Подтверждение нового пароля",
            example = "NewSecurePass456",
            required = true
    )
    private String confirmPassword;
}