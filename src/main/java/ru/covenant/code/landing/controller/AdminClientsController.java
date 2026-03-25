package ru.covenant.code.landing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
