package ru.covenant.code.landing.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.covenant.code.landing.dto.client.request.ClientsRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsCreateRsDto;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.service.client.ClientsService;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Клиенты", description = "Управление заявками клиентов")
public class ClientsController {

    private final ClientsService clientsService;

    @PostMapping
    @Operation(
            summary = "Создать заявку клиента",
            description = "Принимает данные клиента с лендинга, валидирует, сохраняет в БД и отправляет WebSocket-уведомление"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Заявка успешно создана"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            @ApiResponse(responseCode = "409", description = "Заявка с таким email уже существует")
    })
    public ResponseEntity<ResponseWrapper<ClientsCreateRsDto>> createApplication(
            @Valid @RequestBody ClientsRqDto clientsRqDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(clientsService.create(clientsRqDto)));
    }

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
}
