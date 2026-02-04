package ru.covenant.code.landing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.dto.client.request.ClientsStatusRqDto;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.dto.client.response.ClientsStatsRsDto;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.service.client.ClientsService;
import ru.covenant.code.landing.service.client.impl.ClientsServiceImpl;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/clients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('SUPPORT')")
@Tag(name = "Клиенты (Админка)", description = "API для управления клиентами в административной панели")
@SecurityRequirement(name = "bearerAuth")
public class AdminClientsController {

    private final ClientsService clientsService;

    @Operation(
            summary = "Получить список клиентов",
            description = "Возвращает список клиентов с возможностью фильтрации. Доступен для ADMIN, MODERATOR, SUPPORT"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешно получен список клиентов",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<ClientsAdminRsDto>>> getAllClients(
            @Parameter(description = "Параметры фильтрации") ClientsFilterRqDto filter) {
        List<ClientsAdminRsDto> clients = clientsService.getAllClients(filter);
        return ResponseEntity.ok(ResponseWrapper.success(clients));
    }

    @Operation(
            summary = "Получить клиента по ID",
            description = "Возвращает детальную информацию о клиенте по его идентификатору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Клиент найден",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "404", description = "Клиент не найден"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ClientsAdminRsDto>> getClientById(
            @Parameter(description = "UUID клиента", required = true) @PathVariable UUID id) {
        ClientsAdminRsDto client = clientsService.getClientById(id);
        return ResponseEntity.ok(ResponseWrapper.success(client));
    }

    @Operation(
            summary = "Получить клиентов по статусу",
            description = "Возвращает список клиентов с указанным статусом"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешно получен список клиентов",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный статус"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<ResponseWrapper<List<ClientsAdminRsDto>>> getClientsByStatus(
            @Parameter(description = "Статус клиента (NEW, PROCESSED, DONE)", required = true)
            @PathVariable String status) {
        List<ClientsAdminRsDto> clients = clientsService.getClientsByStatus(status);
        return ResponseEntity.ok(ResponseWrapper.success(clients));
    }

    @Operation(
            summary = "Обновить статус клиента",
            description = "Изменяет статус клиента на указанный"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус успешно обновлен",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Клиент не найден"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseWrapper<ClientsAdminRsDto>> updateStatus(
            @Parameter(description = "UUID клиента", required = true) @PathVariable UUID id,
            @Valid @RequestBody ClientsStatusRqDto statusDto) {
        ClientsAdminRsDto client = clientsService.updateStatus(id, statusDto);
        return ResponseEntity.ok(ResponseWrapper.success(client));
    }

    @Operation(
            summary = "Обновить данные клиента",
            description = "Обновляет информацию о клиенте"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные клиента успешно обновлены",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Клиент не найден"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ClientsAdminRsDto>> updateClient(
            @Parameter(description = "UUID клиента", required = true) @PathVariable UUID id,
            @Valid @RequestBody ClientsUpdateRqDto updateDto) {
        ClientsAdminRsDto client = clientsService.updateClient(id, updateDto);
        return ResponseEntity.ok(ResponseWrapper.success(client));
    }

    @Operation(
            summary = "Удалить клиента",
            description = "Удаляет клиента из системы. Доступно только для ADMIN и MODERATOR"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Клиент успешно удален",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "404", description = "Клиент не найден"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ResponseWrapper<Void>> deleteClient(
            @Parameter(description = "UUID клиента", required = true) @PathVariable UUID id) {
        clientsService.delete(id);
        return ResponseEntity.ok(ResponseWrapper.success());
    }

    @Operation(
            summary = "Получить статистику по клиентам",
            description = "Возвращает общую статистику по клиентам"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статистика успешно получена",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/stats")
    public ResponseEntity<ResponseWrapper<ClientsStatsRsDto>> getStats() {
        ClientsStatsRsDto stats = clientsService.getStats();
        return ResponseEntity.ok(ResponseWrapper.success(stats));
    }
}