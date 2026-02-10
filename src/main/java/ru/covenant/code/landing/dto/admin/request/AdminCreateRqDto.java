package ru.covenant.code.landing.dto.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.covenant.code.landing.entity.enumerated.AdminRole;
import ru.covenant.code.landing.validation.constraints.*;

@Data
@PasswordMatch(
        passwordField = "password",
        confirmField = "confirmPassword",
        message = "Пароли не совпадают"
)
@Schema(description = "Запрос на создание администратора")
public class AdminCreateRqDto {

    @NotBlank(message = "Email обязателен")
    @ValidEmail(message = "Некорректный формат email")
    @Schema(
            description = "Email администратора",
            example = "admin@covenantcode.ru",
            required = true
    )
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @ValidPassword(message = "Пароль должен содержать минимум 8 символов, включая цифры и буквы")
    @Schema(
            description = "Пароль администратора",
            example = "SecurePass123",
            required = true,
            minLength = 8
    )
    private String password;

    @NotBlank(message = "Подтверждение пароля обязательно")
    @Schema(
            description = "Подтверждение пароля",
            example = "SecurePass123",
            required = true
    )
    private String confirmPassword;

    @NotBlank(message = "Имя обязательно")
    @ValidName(message = "Имя должно содержать только буквы")
    @Schema(
            description = "Имя администратора",
            example = "Иван",
            required = true,
            minLength = 2
    )
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @ValidName(message = "Фамилия должна содержать только буквы")
    @Schema(
            description = "Фамилия администратора",
            example = "Иванов",
            required = true,
            minLength = 2
    )
    private String lastName;

    @ValidPhone(message = "Неверный формат телефона")
    @Schema(
            description = "Телефон администратора",
            example = "+79161234567"
    )
    private String phone;

    @Schema(
            description = "Отдел/департамент администратора",
            example = "Техническая поддержка"
    )
    private String department;

    @NotBlank(message = "Роль обязательна")
    @ValidEnum(
            enumClass = ru.covenant.code.landing.entity.enumerated.AdminRole.class,
            message = "Недопустимая роль администратора"
    )
    @Schema(
            description = "Роль администратора в системе",
            example = "ADMIN",
            allowableValues = {"SUPER_ADMIN", "ADMIN", "MODERATOR", "SUPPORT"},
            required = true
    )
    private String role;

    public AdminRole getRoleEnum() {
        try {
            return AdminRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AdminRole.ADMIN;
        }
    }
}