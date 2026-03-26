package ru.covenant.code.landing.controller;



import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.covenant.code.landing.dto.client.request.ClientsRqDto;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.covenant.code.landing.dto.client.response.ClientsCreateRsDto;
import ru.covenant.code.landing.dto.client.response.CourseInfoDto;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.error.ErrorResponse;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.exceptions.ClientDuplicateException;
import ru.covenant.code.landing.exceptions.InvalidCourseTypeException;
import ru.covenant.code.landing.service.client.ClientsService;
import ru.covenant.code.landing.util.CourseUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hibernate.query.sqm.tree.SqmNode.log;

@Slf4j
@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Клиенты", description = "Управление заявками клиентов")
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientsController {
    private final ClientsService clientsService;

    @GetMapping("/example")
    @Operation(
            summary = "Тестовый запрос на создание заявки",
            description = "Возвращает пример запроса для создания заявки. Используйте этот JSON как шаблон."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ с примером")
    })
    public ResponseWrapper<ClientsRqDto> getExampleRequest() {
        ClientsRqDto example = ClientsRqDto.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .phone("+79161234567")
                .message("Хотел бы узнать подробнее о курсе по Backend разработке")
                .courseType("BACKEND")
                .source("Лендинг")
                .build();
        return ResponseWrapper.success(example);
    }

    @Operation(summary = "Получить список курсов",
            description = "Возвращает перечень доступных курсов для выбора с их описанием и стоимостью")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный ответ: список курсов",

                    content = @Content
                            (schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "200", description = "Успешный ответ с примером")
    })
    @GetMapping("/courses")
    public ResponseWrapper<List<CourseInfoDto>> getCourseTypes() {
        List<CourseInfoDto> courseInfoDtos = Arrays.stream(CourseType.values())
                .map(courseType -> new CourseInfoDto(
                        courseType.name(),
                        courseType.getDisplayName(),
                        CourseUtil.getCourseDescription(courseType),
                        CourseUtil.getCourseDuration(courseType),
                        CourseUtil.getCoursePrice(courseType)
                ))
                .collect(Collectors.toList());
        return ResponseWrapper.success(courseInfoDtos);
    }

    @PostMapping
    @Operation(summary = "Создание новой заявки",
            description = "Создает новую заявку клиента с лендинга")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Заявка успешно создана",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClientsCreateRsDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации (например, некорректный email или неверный тип курса)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid Email",
                                            summary = "Ошибка валидации email",
                                            value = """
                        {
                          "code": "VALIDATION_ERROR",
                          "description": "Ошибка валидации входных данных",
                          "message": "Имя должно содержать только буквы",
                          "details": {
                            "email": "Некорректный формат email"
                          }
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Course Type",
                                            summary = "Ошибка валидации типа курса",
                                            value = """
                        {
                          "code": "VALIDATION_ERROR",
                          "description": "Ошибка валидации входных данных",
                          "message": "Недопустимый тип курса. Допустимые значения: FULLSTACK, FRONTEND, BACKEND",
                          "details": {
                            "courseType": "Недопустимый тип курса"
                          }
                        }
                        """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ошибка дубликата (email или телефон уже существуют)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Duplicate Client",
                                            summary = "Дубликат клиента по email и телефону",
                                            value = """
                        {
                          "code": "CLIENT_DUPLICATE",
                          "description": "Дубликат заявки",
                          "message": "Заявка с email 'test@example.com' или телефоном '+79001234567' уже существует",
                          "details": {
                            "email": "test@example.com",
                            "phone": "+79001234567"
                          }
                        }
                        """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<ResponseWrapper<ClientsCreateRsDto>> createApplication(
            @RequestBody @Valid ClientsRqDto request
    ) {
        log.info("Получена заявка: {}", request);

        // Валидация типа курса (если вы используете enum)
        if (!isValidCourseType(request.getCourseType())) {
            throw new InvalidCourseTypeException("Неверный тип курса: " + request.getCourseType());
        }

        ClientsCreateRsDto createdClient = clientsService.create(request); // ← Теперь всё чёрное!
        return ResponseEntity.status(201).body(ResponseWrapper.success(createdClient));
    }

    // обработки исключений
    @ExceptionHandler(InvalidCourseTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCourseType(InvalidCourseTypeException ex) {
        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                "Ошибка валидации входных данных",
                ex.getMessage(),
                Map.of("courseType", "Недопустимый тип курса")
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ClientDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleClientDuplicate(ClientDuplicateException ex) {
        ErrorResponse error = new ErrorResponse(
                "CLIENT_DUPLICATE",
                "Дубликат заявки",
                ex.getMessage(),
                Map.of("email", ex.getEmail(), "phone", ex.getPhone())
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }


    private boolean isValidCourseType(String courseType) {
        if (courseType == null) return false;
        return Arrays.stream(CourseType.values())
                .map(Enum::name)
                .anyMatch(type -> type.equals(courseType));
    }

}
