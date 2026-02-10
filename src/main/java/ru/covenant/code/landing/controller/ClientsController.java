package ru.covenant.code.landing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.covenant.code.landing.dto.client.request.ClientsRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsCreateRsDto;
import ru.covenant.code.landing.dto.client.response.LoginStatsRsDto;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.service.client.ClientsService;
import ru.covenant.code.landing.service.client.LoginStatsService;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Клиенты (Публичный API)", description = "Публичный API для работы клиентов с лендингом")
public class ClientsController {

    private final ClientsService clientsService;
    private final LoginStatsService loginStatsService;

    @Operation(
            summary = "Создать заявку",
            description = """
            Создает новую заявку клиента на курс.
            
            ### Обязательные поля:
            - **name**: Имя клиента (2-50 символов, только буквы)
            - **email**: Email клиента (валидный формат)
            - **courseType**: Тип курса (допустимые значения: FULLSTACK, FRONTEND, BACKEND)
            
            ### Необязательные поля:
            - **phone**: Телефон клиента (формат +7XXXXXXXXXX)
            - **message**: Сообщение/комментарий (до 1000 символов)
            - **source**: Источник заявки (по умолчанию "Лендинг")
            
            ### Примеры значений courseType:
            - **FULLSTACK**: Fullstack разработка
            - **FRONTEND**: Frontend разработка  
            - **BACKEND**: Backend разработка
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Заявка успешно создана",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
    })
    @PostMapping
    public ResponseEntity<ResponseWrapper<ClientsCreateRsDto>> createApplication(
            @Valid @RequestBody ClientsRqDto clientsRqDto) {
        ClientsCreateRsDto response = clientsService.create(clientsRqDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(response));
    }

    @Operation(
            summary = "Получить список курсов",
            description = "Возвращает перечень доступных курсов для выбора с их описанием и стоимостью"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список курсов получен",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
    })
    @GetMapping("/courses")
    public ResponseEntity<ResponseWrapper<List<CourseInfo>>> getCourseTypes() {
        List<CourseInfo> courseInfos = Arrays.stream(CourseType.values())
                .map(courseType -> new CourseInfo(
                        courseType.name(),
                        courseType.getDisplayName(),
                        getCourseDescription(courseType),
                        getCourseDuration(courseType),
                        getCoursePrice(courseType)
                ))
                .toList();

        return ResponseEntity.ok(ResponseWrapper.success(courseInfos));
    }

    @Operation(
            summary = "Получить статистику страницы входа",
            description = "Возвращает статистику посещений и конверсий для страницы входа"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статистика получена",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
    })
    @GetMapping("/login/stats")
    public ResponseEntity<ResponseWrapper<LoginStatsRsDto>> getLoginPageStats() {
        LoginStatsRsDto stats = loginStatsService.getLoginPageStats();
        return ResponseEntity.ok(ResponseWrapper.success(stats));
    }

    @Operation(
            summary = "Тестовый запрос на создание заявки",
            description = "Возвращает пример запроса для создания заявки. Используйте этот JSON как шаблон."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пример запроса получен")
    })
    @GetMapping("/example")
    public ResponseEntity<ResponseWrapper<ClientsRqDto>> getExampleRequest() {
        ClientsRqDto example = new ClientsRqDto();
        example.setName("Иван Иванов");
        example.setEmail("ivan@example.com");
        example.setPhone("+79161234567");
        example.setMessage("Хотел бы узнать подробнее о курсе по Backend разработке");
        example.setCourseType("BACKEND");
        example.setSource("Лендинг");

        return ResponseEntity.ok(ResponseWrapper.success(example));
    }

    private String getCourseDescription(CourseType courseType) {
        return switch (courseType) {
            case FULLSTACK -> "Полный цикл разработки веб-приложений: фронтенд и бэкенд";
            case FRONTEND -> "Разработка пользовательских интерфейсов и клиентской части";
            case BACKEND -> "Разработка серверной логики и API";
        };
    }

    private String getCourseDuration(CourseType courseType) {
        return switch (courseType) {
            case FULLSTACK -> "6 месяцев";
            case FRONTEND -> "4 месяца";
            case BACKEND -> "5 месяцев";
        };
    }

    private String getCoursePrice(CourseType courseType) {
        return switch (courseType) {
            case FULLSTACK -> "35 000 ₽";
            case FRONTEND -> "25 000 ₽";
            case BACKEND -> "30 000 ₽";
        };
    }

    @Schema(description = "Информация о курсе")
    private record CourseInfo(
            @Schema(description = "Код курса", example = "BACKEND")
            String code,

            @Schema(description = "Название курса", example = "Backend разработка")
            String name,

            @Schema(description = "Описание курса", example = "Разработка серверной логики и API")
            String description,

            @Schema(description = "Длительность курса", example = "5 месяцев")
            String duration,

            @Schema(description = "Стоимость курса", example = "30 000 ₽")
            String price
    ) {}
}