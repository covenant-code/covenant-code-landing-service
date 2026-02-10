package ru.covenant.code.landing.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.covenant.code.landing.dto.admin.request.AdminLoginRqDto;
import ru.covenant.code.landing.dto.admin.response.AdminLoginRsDto;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.service.admin.impl.AdminUserServiceImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для аутентификации администраторов")
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final AdminUserServiceImpl adminUserService;

    @Operation(
            summary = "Аутентификация администратора (API)",
            description = """
            Аутентификация администратора через REST API. Возвращает JSON ответ.
            
            После успешной аутентификации:
            1. Устанавливается cookie JSESSIONID
            2. Создается сессия на сервере
            3. Для последующих запросов используйте тот же JSESSIONID
            
            ### CSRF защита отключена для этого endpoint
            ### Для веб-интерфейса используйте /login форму
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная аутентификация",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверные учетные данные",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<AdminLoginRsDto>> login(
            @Valid @RequestBody AdminLoginRqDto loginDto) {
        try {
            // Spring Security сам проверит логин/пароль через UserDetailsService
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Обновляем время последнего входа
            adminUserService.updateLastLogin(loginDto.getEmail());

            // Получаем роль пользователя
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .orElse("USER");

            AdminLoginRsDto response = AdminLoginRsDto.builder()
                    .success(true)
                    .message("Авторизация успешна")
                    .authenticated(true)
                    .email(loginDto.getEmail())
                    .role(role)
                    .build();

            return ResponseEntity.ok(ResponseWrapper.success(response));

        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseWrapper.error(
                            "AUTHENTICATION_FAILED",
                            "Неверные учетные данные",
                            "Проверьте email и пароль"
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseWrapper.error(
                            "AUTHENTICATION_ERROR",
                            "Ошибка аутентификации",
                            e.getMessage()
                    ));
        }
    }

    @Operation(
            summary = "Выход из системы (API)",
            description = "Завершает текущую сессию пользователя через REST API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный выход из системы",
                    content = @Content(schema = @Schema(implementation = ResponseWrapper.class))
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<ResponseWrapper<Void>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ResponseWrapper.success());
    }
}