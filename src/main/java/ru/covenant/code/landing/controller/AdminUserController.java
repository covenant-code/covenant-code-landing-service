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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.covenant.code.landing.dto.admin.request.AdminCreateRqDto;
import ru.covenant.code.landing.dto.admin.request.AdminFilterRqDto;
import ru.covenant.code.landing.dto.admin.request.AdminPasswordRqDto;
import ru.covenant.code.landing.dto.admin.request.AdminUpdateRqDto;
import ru.covenant.code.landing.dto.admin.response.AdminRsDto;
import ru.covenant.code.landing.dto.admin.response.AdminStatsRsDto;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.service.admin.AdminUserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
@Tag(name = "Администраторы", description = "API для управления администраторами системы")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(
            summary = "Получить список администраторов",
            description = "Возвращает постраничный список администраторов с возможностью сортировки и фильтрации"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешно получен список администраторов",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping
    public ResponseEntity<ResponseWrapper<Page<AdminRsDto>>> getAllAdmins(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Поле для сортировки", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Направление сортировки (asc/desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortOrder,

            @Parameter(description = "Параметры фильтрации") AdminFilterRqDto filter) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AdminRsDto> admins = adminUserService.getAllAdmins(filter, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(admins));
    }

    @Operation(
            summary = "Получить список администраторов с фильтром (POST)",
            description = "Альтернативный метод для получения списка администраторов с фильтром через тело запроса"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешно получен список администраторов",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные фильтра"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @PostMapping("/filter")
    public ResponseEntity<ResponseWrapper<Page<AdminRsDto>>> getAllAdminsWithFilter(
            @Valid @RequestBody AdminFilterRqDto filter,

            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AdminRsDto> admins = adminUserService.getAllAdmins(filter, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(admins));
    }

    @Operation(
            summary = "Получить администратора по ID",
            description = "Возвращает детальную информацию об администраторе по его идентификатору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Администратор найден",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "404", description = "Администратор не найден"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AdminRsDto>> getAdminById(
            @Parameter(description = "UUID администратора", required = true) @PathVariable UUID id) {
        AdminRsDto admin = adminUserService.getAdminById(id);
        return ResponseEntity.ok(ResponseWrapper.success(admin));
    }

    @Operation(
            summary = "Создать нового администратора",
            description = "Создает нового администратора системы. Доступно только для SUPER_ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Администратор успешно создан",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "409", description = "Администратор с таким email уже существует"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ResponseWrapper<AdminRsDto>> createAdmin(
            @Valid @RequestBody AdminCreateRqDto createDto) {
        AdminRsDto admin = adminUserService.createAdmin(createDto);
        return ResponseEntity.ok(ResponseWrapper.success(admin));
    }

    @Operation(
            summary = "Обновить данные администратора",
            description = "Обновляет информацию об администраторе"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные администратора успешно обновлены",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Администратор не найден"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<AdminRsDto>> updateAdmin(
            @Parameter(description = "UUID администратора", required = true) @PathVariable UUID id,
            @Valid @RequestBody AdminUpdateRqDto updateDto) {
        AdminRsDto admin = adminUserService.updateAdmin(id, updateDto);
        return ResponseEntity.ok(ResponseWrapper.success(admin));
    }

    @Operation(
            summary = "Удалить администратора",
            description = "Удаляет администратора из системы. Доступно только для SUPER_ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Администратор успешно удален",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "404", description = "Администратор не найден"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ResponseWrapper<Void>> deleteAdmin(
            @Parameter(description = "UUID администратора", required = true) @PathVariable UUID id) {
        adminUserService.deleteAdmin(id);
        return ResponseEntity.ok(ResponseWrapper.success());
    }

    @Operation(
            summary = "Изменить пароль администратора",
            description = "Изменяет пароль администратора"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пароль успешно изменен",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Администратор не найден"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @PutMapping("/{id}/password")
    public ResponseEntity<ResponseWrapper<Void>> changePassword(
            @Parameter(description = "UUID администратора", required = true) @PathVariable UUID id,
            @Valid @RequestBody AdminPasswordRqDto passwordDto) {
        adminUserService.changePassword(id, passwordDto);
        return ResponseEntity.ok(ResponseWrapper.success());
    }

    @Operation(
            summary = "Переключить статус администратора",
            description = "Активирует/деактивирует администратора. Доступно только для SUPER_ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус успешно изменен",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "404", description = "Администратор не найден"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ResponseWrapper<AdminRsDto>> toggleAdminStatus(
            @Parameter(description = "UUID администратора", required = true) @PathVariable UUID id) {
        AdminRsDto admin = adminUserService.toggleAdminStatus(id);
        return ResponseEntity.ok(ResponseWrapper.success(admin));
    }

    @Operation(
            summary = "Получить статистику по администраторам",
            description = "Возвращает статистику по администраторам системы"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статистика успешно получена",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/stats")
    public ResponseEntity<ResponseWrapper<AdminStatsRsDto>> getAdminStats() {
        AdminStatsRsDto stats = adminUserService.getAdminStats();
        return ResponseEntity.ok(ResponseWrapper.success(stats));
    }

    @Operation(
            summary = "Получить текущего администратора",
            description = "Возвращает информацию о текущем аутентифицированном администраторе"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Информация получена",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/me")
    public ResponseEntity<ResponseWrapper<AdminRsDto>> getCurrentAdmin() {
        // Получаем текущего пользователя из SecurityContext
        // Реализация зависит от вашей аутентификации
        // TODO: Реализовать получение текущего пользователя
        throw new RuntimeException("Not implemented yet");
    }
}