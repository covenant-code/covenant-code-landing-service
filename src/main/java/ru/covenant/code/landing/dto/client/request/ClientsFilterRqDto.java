package ru.covenant.code.landing.dto.client.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.entity.enumerated.Priority;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Schema(description = "Фильтр для поиска клиентов")
public class ClientsFilterRqDto {

    @Schema(
            description = "Начальная дата для фильтрации",
            example = "2024-01-01"
    )
    private LocalDate startDate;

    @Schema(
            description = "Конечная дата для фильтрации",
            example = "2024-01-31"
    )
    private LocalDate endDate;

    @Schema(
            description = "Список статусов для фильтрации",
            example = "[\"NEW\", \"PROCESSED\"]"
    )
    private List<Status> statuses;

    @Schema(
            description = "Список приоритетов для фильтрации",
            example = "[\"HIGH\", \"MEDIUM\"]"
    )
    private List<Priority> priorities;

    @Schema(
            description = "Список типов курсов для фильтрации",
            example = "[\"BACKEND\", \"FRONTEND\"]"
    )
    private List<CourseType> courseTypes;

    @Schema(
            description = "Поисковый запрос (по имени, email, телефону или сообщению)",
            example = "Иван"
    )
    private String searchQuery;
}