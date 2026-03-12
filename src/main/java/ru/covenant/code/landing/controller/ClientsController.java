package ru.covenant.code.landing.controller;



import io.swagger.v3.oas.annotations.media.Content;
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
import ru.covenant.code.landing.dto.client.response.CourseInfoDto;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.util.CourseUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Клиенты", description = "Управление заявками клиентов")
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientsController {

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
}
