package ru.covenant.code.landing.dto.client.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.validation.constraints.ValidEmail;
import ru.covenant.code.landing.validation.constraints.ValidEnum;
import ru.covenant.code.landing.validation.constraints.ValidName;
import ru.covenant.code.landing.validation.constraints.ValidPhone;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание заявки клиента")
public class ClientsRqDto {

    @NotBlank(message = "{clients.name.notblank}")
    @ValidName
    @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
    @Schema(description = "Имя клиента", example = "Иван Иванов")
    private String name;

    @NotBlank(message = "{clients.email.notblank}")
    @ValidEmail
    @Schema(description = "Email клиента", example = "ivan@example.com")
    private String email;

    @ValidPhone
    @Schema(description = "Телефон в формате +7XXXXXXXXXX", example = "+79161234567")
    private String phone;

    @Size(max = 1000, message = "Сообщение не должно превышать 1000 символов")
    @Schema(description = "Сообщение клиента", example = "Хотел бы узнать подробнее о курсе по Backend разработке")
    private String message;

    @NotBlank(message = "{clients.courseType.notblank}")
    @ValidEnum(enumClass = CourseType.class, message = "Недопустимый тип курса. Допустимые значения: FULLSTACK, FRONTEND, BACKEND")
    @Schema(description = "Тип курса", example = "BACKEND")
    private String courseType;

    @Schema(description = "Источник заявки", example = "Лендинг", defaultValue = "Лендинг")
    private String source;

    public CourseType getCourseTypeEnum() {
        if (courseType == null) return null;
        return CourseType.valueOf(courseType.toUpperCase());
    }
}
