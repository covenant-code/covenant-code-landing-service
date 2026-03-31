package ru.covenant.code.landing.dto.client.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.validation.constraints.ValidDateRange;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "Фильтр для получения списка клиентов")
@ValidDateRange(
        startDateField = "startDate",
        endDateField = "endDate",
        message = "Конечная дата должна быть позже или равна начальной"
)
public class ClientsFilterRqDto {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(
            description = "Начальная дата для фильтрации (включительно)",
            example = "2024-01-01",
            format = "date"
    )
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(
            description = "Конечная дата для фильтрации (включительно)",
            example = "2024-12-31",
            format = "date"
    )
    private LocalDate endDate;

    @Schema(
            description = "Список статусов через запятую",
            example = "NEW,PROCESSED",
            type = "string"
    )
    private List<Status> statuses;

    @Schema(
            description = "Список приоритетов через запятую",
            example = "HIGH,MEDIUM",
            type = "string"
    )
    private List<Priority> priorities;

    @Schema(
            description = "Список типов курсов через запятую",
            example = "BACKEND,FRONTEND",
            type = "string"
    )
    private List<CourseType> courseTypes;

    @Schema(
            description = "Поиск по имени, email или телефону (регистронезависимый)",
            example = "Иван"
    )
    private String searchQuery;
}