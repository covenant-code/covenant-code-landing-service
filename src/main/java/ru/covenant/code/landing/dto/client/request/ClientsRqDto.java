package ru.covenant.code.landing.dto.client.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.validation.constraints.*;

@Getter
@Setter
@Schema(description = "Запрос на создание заявки клиента")
public class ClientsRqDto {

    @NotBlank(message = "Имя обязательно")
    @ValidName(message = "Имя должно содержать только буквы")
    @Schema(
            description = "Имя клиента",
            example = "Иван",
            required = true,
            minLength = 2,
            maxLength = 50
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
            example = "Хотел бы узнать подробнее о курсе",
            maxLength = 1000
    )
    private String message;

    @NotNull(message = "Тип курса обязателен")
    @ValidEnum(
            enumClass = ru.covenant.code.landing.entity.enumerated.CourseType.class,
            message = "Недопустимый тип курса. Допустимые значения: FULLSTACK, FRONTEND, BACKEND"
    )
    @Schema(
            description = "Тип выбранного курса",
            example = "BACKEND",
            required = true,
            allowableValues = {"FULLSTACK", "FRONTEND", "BACKEND"}
    )
    private String courseType;

    @Schema(
            description = "Источник заявки",
            example = "Лендинг",
            defaultValue = "Лендинг"
    )
    private String source = "Лендинг";

    public CourseType getCourseTypeEnum() {
        try {
            return CourseType.valueOf(courseType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}