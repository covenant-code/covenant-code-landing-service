package ru.covenant.code.landing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.service.client.ClientsService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/clients")
@RequiredArgsConstructor
@Tag(name = "Админка: Клиенты", description = "Управление заявками клиентов для администраторов")
@SecurityRequirement(name = "bearerAuth")
public class AdminClientsController {

    private final ClientsService clientsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'SUPPORT')")
    @Operation(
            summary = "Получение списка клиентов с фильтрацией",
            description = """
                    Возвращает список клиентов с возможностью фильтрации:
                    - по датам (startDate, endDate)
                    - по статусам (statuses)
                    - по приоритетам (priorities)
                    - по типам курсов (courseTypes)
                    - текстовый поиск (searchQuery)
                    
                    Доступно только для ADMIN, MODERATOR и SUPPORT.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ"),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса (например, endDate раньше startDate)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    public ResponseWrapper<List<ClientsAdminRsDto>> getAllClients(
            @Parameter(description = "Параметры фильтрации клиентов")
            ClientsFilterRqDto filter
    ) {
        List<ClientsAdminRsDto> clients = clientsService.getAllClients(filter);
        return ResponseWrapper.success(clients);
    }


        @GetMapping("/status/{status}")
        @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'SUPPORT')")
        @Operation(
                summary = "Получить клиентов по статусу",
                description = "Возвращает список клиентов с указанным статусом"
        )
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Успешно получен список клиентов"),
                @ApiResponse(responseCode = "400", description = "Некорректный статус"),
                @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
                @ApiResponse(responseCode = "403", description = "Недостаточно прав")
        })
        public ResponseWrapper<List<ClientsAdminRsDto>> getClientsByStatus(
                @Parameter(
                        description = "Статус клиента (NEW, PROCESSED, DONE)",
                        example = "NEW",
                        required = true,
                        schema = @Schema(
                                allowableValues = {"NEW", "PROCESSED", "DONE"},
                                type = "string"
                        )
                )
                @PathVariable String status) {

            List<ClientsAdminRsDto> clients = clientsService.getClientsByStatus(status);
            return ResponseWrapper.success(clients);
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'SUPPORT')")
        @Operation(
                summary = "Обновить данные клиента",
                description = """
                    Обновляет информацию о клиенте.
                    При указании processedBy автоматически устанавливается processedAt.
                    Null значения в запросе игнорируются.
                    
                    Доступно для ADMIN, MODERATOR, SUPPORT.
                    """
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Клиент успешно обновлен"),
                @ApiResponse(responseCode = "400", description = "Неверные данные запроса"),
                @ApiResponse(responseCode = "401", description = "Не авторизован"),
                @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                @ApiResponse(responseCode = "404", description = "Клиент не найден"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        })
        public ResponseWrapper<ClientsAdminRsDto> updateClient (
                @Parameter(
                        description = "",
                        required = true,
                        example = "123e4567-e89b-12d3-a456-426614174000"
                )
                @PathVariable UUID id,
                @Parameter(description = "Данные для обновления клиента")
                @Valid @RequestBody ClientsUpdateRqDto updateDto)
        {
            ClientsAdminRsDto updatedClient = clientsService.updateClient(id, updateDto);
            return ResponseWrapper.success(updatedClient);
        }



    @GetMapping("/{id}")
    @Operation(
            summary = "Получить клиента по ID",
            description = "Возвращает детальную информацию о клиенте по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Клиент найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples = @ExampleObject(
                                    name = "successResponse",
                                    summary = "Успешный ответ с данными клиента",
                                    value = """
                                            {
                                                "success": true,
                                                "result": {
                                                    "id": "7da674d5-0672-4d0b-a7d3-8f4ee5d3a434",
                                                    "email": "client@example.com",
                                                    "firstName": "Иван",
                                                    "lastName": "Петров",
                                                    "middleName": "Сергеевич",
                                                    "phone": "+7 (999) 123-45-67",
                                                    "status": "ACTIVE",
                                                    "createdAt": "2026-03-17T10:30:00Z",
                                                    "updatedAt": "2026-03-17T10:30:00Z"
                                                },
                                                "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Требуется аутентификация",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples = @ExampleObject(
                                    name = "unauthorizedResponse",
                                    summary = "Ошибка аутентификации",
                                    value = """
                                            {
                                                "success": false,
                                                "result": null,
                                                "error": {
                                                    "code": "UNAUTHORIZED",
                                                    "description": "Требуется аутентификация",
                                                    "message": "Отсутствует или недействителен токен авторизации",
                                                    "details": null
                                                }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples = @ExampleObject(
                                    name = "forbiddenResponse",
                                    summary = "Ошибка доступа",
                                    value = """
                                            {
                                                "success": false,
                                                "result": null,
                                                "error": {
                                                    "code": "ACCESS_DENIED",
                                                    "description": "Недостаточно прав",
                                                    "message": "У вас нет прав для доступа к этому ресурсу",
                                                    "details": {
                                                        "requiredRole": "ADMIN",
                                                        "userRole": "USER"
                                                    }
                                                }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Клиент не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples = @ExampleObject(
                                    name = "notFoundResponse",
                                    summary = "Клиент не найден",
                                    value = """
                                            {
                                                "success": false,
                                                "result": null,
                                                "error": {
                                                    "code": "CLIENT_NOT_FOUND",
                                                    "description": "Клиент не найден",
                                                    "message": "Клиент с ID {7da674d5-0672-4d0b-a7d3-8f4ee5d3a434} не найден",
                                                    "details": {
                                                        "clientId": "7da674d5-0672-4d0b-a7d3-8f4ee5d3a434"
                                                    }
                                                }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат UUID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples = @ExampleObject(
                                    name = "badRequestResponse",
                                    summary = "Неверный формат идентификатора",
                                    value = """
                                            {
                                                "success": false,
                                                "result": null,
                                                "error": {
                                                    "code": "INVALID_UUID",
                                                    "description": "Неверный формат UUID",
                                                    "message": "Переданный идентификатор имеет неверный формат",
                                                    "details": {
                                                        "invalidValue": "12345",
                                                        "expectedFormat": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                                                    }
                                                }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples = @ExampleObject(
                                    name = "internalErrorResponse",
                                    summary = "Внутренняя ошибка сервера",
                                    value = """
                                            {
                                                "success": false,
                                                "result": null,
                                                "error": {
                                                    "code": "INTERNAL_SERVER_ERROR",
                                                    "description": "Внутренняя ошибка сервера",
                                                    "message": "Произошла непредвиденная ошибка",
                                                    "details": null
                                                }
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseWrapper<ClientsAdminRsDto> getClientById(@PathVariable UUID id) {
        log.info("REST Получение клиента по id: {}", id);
        ClientsAdminRsDto client = clientsService.getClientById(id);
        return ResponseWrapper.success(client);
    }
}
