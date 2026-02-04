package ru.covenant.code.landing.dto.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.covenant.code.landing.validation.constraints.*;

@Data
@Schema(description = "Запрос на обновление данных администратора")
public class AdminUpdateRqDto {

    @NotBlank(message = "Имя обязательно")
    @ValidName(message = "Имя должно содержать только буквы")
    @Schema(
            description = "Имя администратора",
            example = "Иван",
            required = true
    )
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @ValidName(message = "Фамилия должна содержать только буквы")
    @Schema(
            description = "Фамилия администратора",
            example = "Иванов",
            required = true
    )
    private String lastName;

    @ValidEmail(message = "Некорректный формат email")
    @Schema(
            description = "Email администратора",
            example = "admin@covenantcode.ru"
    )
    private String email;

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

    @ValidEnum(
            enumClass = ru.covenant.code.landing.entity.enumerated.AdminRole.class,
            message = "Недопустимая роль администратора",
            nullable = true
    )
    @Schema(
            description = "Роль администратора",
            example = "MODERATOR",
            allowableValues = {"SUPER_ADMIN", "ADMIN", "MODERATOR", "SUPPORT"}
    )
    private String role;

    @Schema(
            description = "Статус активности администратора",
            example = "true"
    )
    private Boolean active;
}