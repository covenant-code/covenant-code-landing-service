package ru.covenant.code.landing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.covenant.code.landing.dto.admin.response.AdminRsDto;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.service.admin.AdminUserService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Текущий пользователь", description = "API для получения информации о текущем аутентифицированном пользователе")
@SecurityRequirement(name = "bearerAuth")
public class CurrentUserController {

    private final AdminUserService adminUserService;

    @Operation(
            summary = "Получить текущего пользователя",
            description = "Возвращает информацию о текущем аутентифицированном администраторе"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о пользователе получена",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не аутентифицирован",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))
            )
    })
    @GetMapping("/me")
    public ResponseEntity<ResponseWrapper<AdminRsDto>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity
                    .status(401)
                    .body(ResponseWrapper.error(
                            "UNAUTHORIZED",
                            "Пользователь не аутентифицирован"
                    ));
        }

        String email = authentication.getName();
        AdminRsDto admin = adminUserService.getAdminByEmail(email);

        return ResponseEntity.ok(ResponseWrapper.success(admin));
    }

    @Operation(
            summary = "Проверить аутентификацию",
            description = "Проверяет, аутентифицирован ли текущий пользователь"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус аутентификации получен"
            )
    })
    @GetMapping("/check-auth")
    public ResponseEntity<ResponseWrapper<Boolean>> checkAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();

        return ResponseEntity.ok(ResponseWrapper.success(isAuthenticated));
    }
}