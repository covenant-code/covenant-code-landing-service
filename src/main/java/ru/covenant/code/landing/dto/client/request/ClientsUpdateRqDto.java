package ru.covenant.code.landing.dto.client.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.validation.constraints.ValidEmail;
import ru.covenant.code.landing.validation.constraints.ValidEnum;
import ru.covenant.code.landing.validation.constraints.ValidName;
import ru.covenant.code.landing.validation.constraints.ValidPhone;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Обновления данных клиента")
public class ClientsUpdateRqDto {

    @NotBlank(message = "{clients.name.notblank}")
    @ValidName
    @Schema(description = "Имя клиента", example = "Иван")
    private String name;

    @NotBlank
    @ValidEmail
    @Schema(description = "Email клиента", example = "ivan@example.com")
    private String email;

    @ValidPhone
    @Schema(description = "Телефон клиента", example = "+79161234567")
    private String phone;

    @ValidEnum(enumClass = CourseType.class)
    @Schema(description = "Тип курса", example = "BACKEND")
    private String courseType;

    @ValidEnum(enumClass = Status.class)
    @Schema(description = "Статус заявки", example = "PROCESSED")
    private String status;

    @ValidEnum(enumClass = Priority.class)
    @Schema(description = "Приоритет заявки", example = "MEDIUM")
    private String priority;

    @Size(max = 100, message = "Максимальная длинны сообщения 100 символов")
    @Schema(description = "Сообщение/комментарий", example = "Хочу узнать детали курса")
    private String message;

    @Schema(description = "Источник заявки", example = "Лендинг")
    private String source;

    @Schema(description = "Администратор", example = "admin@covenantcode.ru")
    private String processedBy;
}
