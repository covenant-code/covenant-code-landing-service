package ru.covenant.code.landing.dto.client.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание заявки клиента")
public class ClientsRqDto {

    @NotBlank(message = "{clients.name.notblank}")
    @Schema(description = "Имя клиента", example = "Иван Иванов")
    private String name;

    @NotBlank(message = "{clients.email.notblank}")
    @Email(message = "{clients.email.invalid}")
    @Schema(description = "Email клиента", example = "ivan@example.com")
    private String email;

    @Pattern(regexp = "^\\+7[0-9]{10}$", message = "{clients.phone.invalid}")
    @Schema(description = "Телефон в формате +7XXXXXXXXXX", example = "+79161234567")
    private String phone;

    @Schema(description = "Сообщение клиента", example = "Хотел бы узнать подробнее о курсе по Backend разработке")
    private String message;

    @NotBlank(message = "{clients.courseType.notblank}")
    @Schema(description = "Тип курса", example = "BACKEND")
    private String courseType;

    @Schema(description = "Источник заявки", example = "Лендинг", defaultValue = "Лендинг")
    private String source;
}