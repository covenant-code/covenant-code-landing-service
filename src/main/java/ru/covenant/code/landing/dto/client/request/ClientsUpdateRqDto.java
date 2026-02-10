package ru.covenant.code.landing.dto.client.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import ru.covenant.code.landing.validation.constraints.*;

@Getter
@Setter
@Schema(description = "Запрос на обновление данных клиента")
public class ClientsUpdateRqDto {

    @NotBlank(message = "Имя обязательно")
    @ValidName(message = "Имя должно содержать только буквы")
    @Schema(
            description = "Имя клиента",
            example = "Иван",
            required = true
    )
    private String name;

    @NotBlank(message = "Email обязателен")
    @ValidEmail(message = "Неверный формат email")
    @Schema(
            description = "Email клиента",
            example = "ivan@example.com",
            required = true
    )
    private String email;

    @ValidPhone(message = "Неверный формат телефона")
    @Schema(
            description = "Телефон клиента",
            example = "+79161234567"
    )
    private String phone;

    @Schema(
            description = "Сообщение/комментарий клиента",
            example = "Обновленный комментарий",
            maxLength = 1000
    )
    private String message;

    @ValidEnum(
            enumClass = ru.covenant.code.landing.entity.enumerated.CourseType.class,
            message = "Недопустимый тип курса. Допустимые значения: FULLSTACK, FRONTEND, BACKEND"
    )
    @Schema(
            description = "Тип курса",
            example = "BACKEND",
            allowableValues = {"FULLSTACK", "FRONTEND", "BACKEND"}
    )
    private String courseType;

    @ValidEnum(
            enumClass = ru.covenant.code.landing.entity.enumerated.Status.class,
            message = "Недопустимый статус. Допустимые значения: NEW, PROCESSED, DONE"
    )
    @Schema(
            description = "Статус клиента",
            example = "PROCESSED",
            allowableValues = {"NEW", "PROCESSED", "DONE"}
    )
    private String status;

    @ValidEnum(
            enumClass = ru.covenant.code.landing.entity.enumerated.Priority.class,
            message = "Недопустимый приоритет. Допустимые значения: HIGH, MEDIUM, LOW"
    )
    @Schema(
            description = "Приоритет заявки",
            example = "MEDIUM",
            allowableValues = {"HIGH", "MEDIUM", "LOW"}
    )
    private String priority;

    @Schema(
            description = "Источник заявки",
            example = "Лендинг"
    )
    private String source;

    @Schema(
            description = "Администратор, обработавший заявку",
            example = "admin@covenantcode.ru"
    )
    private String processedBy;

    public ru.covenant.code.landing.entity.enumerated.CourseType getCourseTypeEnum() {
        if (courseType == null) {
            return null;
        }
        try {
            return ru.covenant.code.landing.entity.enumerated.CourseType.valueOf(
                    courseType.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public ru.covenant.code.landing.entity.enumerated.Status getStatusEnum() {
        if (status == null) {
            return null;
        }
        try {
            return ru.covenant.code.landing.entity.enumerated.Status.valueOf(
                    status.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public ru.covenant.code.landing.entity.enumerated.Priority getPriorityEnum() {
        if (priority == null) {
            return null;
        }
        try {
            return ru.covenant.code.landing.entity.enumerated.Priority.valueOf(
                    priority.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}