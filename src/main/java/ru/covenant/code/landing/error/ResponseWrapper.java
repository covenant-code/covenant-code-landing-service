package ru.covenant.code.landing.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Обертка для всех ответов API")
@FieldDefaults(makeFinal = false, level = AccessLevel.PRIVATE)
public class ResponseWrapper<T> {
    @Schema(description = "Флаг успешности выполнения запроса", example = "true")
    boolean success;

    @Schema(description = "Данные ответа")
    T result;

    @Schema(description = "Информация об ошибке (если success = false)")
    ErrorResponse error;

    public static <T> ResponseWrapper<T> success(T result) {
        return ResponseWrapper.<T>builder().
                success(true)
                .result(result)
                .build();
    }

    public static ResponseWrapper<Void> success() {
        return ResponseWrapper.<Void>builder()
                .success(true)
                .build();
    }

    public static <T> ResponseWrapper<T> error(ErrorResponse error) {
        return ResponseWrapper.<T>builder()
                .success(false)
                .error(error)
                .build();
    }

    public static <T> ResponseWrapper<T> error(String code, String description, String message) {
        return ResponseWrapper.<T>builder()
                .success(false)
                .error(ErrorResponse.of(code, description, message))
                .build();
    }

    public static <T> ResponseWrapper<T> error(String code, String description) {
        return ResponseWrapper.<T>builder()
                .success(false)
                .error(ErrorResponse.of(code, description))
                .build();
    }




}
