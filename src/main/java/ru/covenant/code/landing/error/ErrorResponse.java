package ru.covenant.code.landing.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Модель ошибки API")
public class ErrorResponse {

    @Schema(
            description = "Код ошибки",
            example = "VALIDATION_ERROR"
    )
    private String code;

    @Schema(
            description = "Описание ошибки",
            example = "Ошибка валидации входных данных"
    )
    private String description;

    @Schema(
            description = "Подробное сообщение об ошибке",
            example = "Имя должно содержать только буквы"
    )
    private String message;

    @Schema(
            description = "Детальная информация об ошибке"
    )
    private Object details;

    public static ErrorResponse of(String code, String description, String message) {
        return ErrorResponse.builder()
                .code(code)
                .description(description)
                .message(message)
                .build();
    }

    public static ErrorResponse of(String code, String description, String message, Object details) {
        return ErrorResponse.builder()
                .code(code)
                .description(description)
                .message(message)
                .details(details)
                .build();
    }

    public static ErrorResponse of(String code, String description) {
        return ErrorResponse.builder()
                .code(code)
                .description(description)
                .build();
    }
}