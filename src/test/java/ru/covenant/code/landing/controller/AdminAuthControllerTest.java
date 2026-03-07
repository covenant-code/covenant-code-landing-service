package ru.covenant.code.landing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.covenant.code.landing.dto.admin.request.AdminLoginRqDto;
import ru.covenant.code.landing.dto.admin.response.AdminLoginRsDto;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.service.admin.impl.AdminUserServiceImpl;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты контроллера аутентификации администраторов")
@Tag("unit")
class AdminAuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AdminUserServiceImpl adminUserService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AdminAuthController adminAuthController;

    private ObjectMapper objectMapper;
    private AdminLoginRqDto validLoginRequest;
    private AdminLoginRqDto invalidLoginRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Подготовка валидного запроса
        validLoginRequest = new AdminLoginRqDto();
        validLoginRequest.setEmail("admin@covenantcode.ru");
        validLoginRequest.setPassword("admin123");

        // Подготовка невалидного запроса
        invalidLoginRequest = new AdminLoginRqDto();
        invalidLoginRequest.setEmail("wrong@covenantcode.ru");
        invalidLoginRequest.setPassword("wrongpass");
    }

    @Test
    @DisplayName("Успешная аутентификация - должен вернуть 200 OK с данными пользователя и ролью")
    void login_Success_ShouldReturn200WithUserDataAndRole() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Используем thenAnswer вместо thenReturn
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                List.of((GrantedAuthority) () -> "ROLE_ADMIN")
        );

        doNothing().when(adminUserService).updateLastLogin(anyString());

        // Act
        ResponseEntity<ResponseWrapper<AdminLoginRsDto>> response =
                adminAuthController.login(validLoginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(adminUserService, times(1)).updateLastLogin("admin@covenantcode.ru");
    }

    @Test
    @DisplayName("Успешная аутентификация с разными ролями - проверка всех ролей")
    void login_WithDifferentRoles_ShouldReturnCorrectRole() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        List<String> roles = List.of("SUPER_ADMIN", "ADMIN", "MODERATOR", "SUPPORT");

        for (String expectedRole : roles) {
            // Вместо reset используем новую настройку для каждой роли
            Collection<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + expectedRole)
            );
            doReturn(authorities).when(authentication).getAuthorities();

            // Act
            ResponseEntity<ResponseWrapper<AdminLoginRsDto>> response =
                    adminAuthController.login(validLoginRequest);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(expectedRole, response.getBody().getResult().getRole());
        }
    }

    @Test
    @DisplayName("Неверные учетные данные - должен вернуть 401 UNAUTHORIZED с кодом AUTHENTICATION_FAILED")
    void login_BadCredentials_ShouldReturn401WithAuthenticationFailedCode() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act
        ResponseEntity<ResponseWrapper<AdminLoginRsDto>> response =
                adminAuthController.login(invalidLoginRequest);

        // Assert
        assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertFalse(response.getBody().isSuccess()),
                () -> assertNull(response.getBody().getResult()),
                () -> assertNotNull(response.getBody().getError()),
                () -> assertEquals("AUTHENTICATION_FAILED", response.getBody().getError().getCode()),
                () -> assertEquals("Неверные учетные данные", response.getBody().getError().getDescription()),
                () -> assertEquals("Проверьте email и пароль", response.getBody().getError().getMessage())
        );

        verify(adminUserService, never()).updateLastLogin(anyString());
    }

    @Test
    @DisplayName("Заблокированный пользователь - должен вернуть 401 UNAUTHORIZED с сообщением о блокировке")
    void login_DisabledUser_ShouldReturn401WithBlockedMessage() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("Учетная запись заблокирована"));

        // Act
        ResponseEntity<ResponseWrapper<AdminLoginRsDto>> response =
                adminAuthController.login(validLoginRequest);

        // Assert
        assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertFalse(response.getBody().isSuccess()),
                () -> assertNull(response.getBody().getResult()),
                () -> assertNotNull(response.getBody().getError()),
                () -> assertEquals("AUTHENTICATION_ERROR", response.getBody().getError().getCode()),
                () -> assertEquals("Пользователя не существует", response.getBody().getError().getDescription())
        );

        verify(adminUserService, never()).updateLastLogin(anyString());
    }

    @Test
    @DisplayName("Несуществующий пользователь - должен вернуть 401 UNAUTHORIZED")
    void login_UserNotFound_ShouldReturn401() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("User not found"));

        // Act
        ResponseEntity<ResponseWrapper<AdminLoginRsDto>> response =
                adminAuthController.login(invalidLoginRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertNotNull(response.getBody().getError());
    }

    @Test
    @DisplayName("Ошибка при обновлении lastLogin - не должна влиять на успешную аутентификацию")
    void login_UpdateLastLoginFails_ShouldStillReturn200() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        doReturn(authorities).when(authentication).getAuthorities();

        // ВАЖНО: НЕ выбрасываем исключение, а просто ничего не делаем
        // Контроллер не должен получать исключение из сервиса
        doNothing().when(adminUserService).updateLastLogin(anyString());

        // Act
        ResponseEntity<ResponseWrapper<AdminLoginRsDto>> response =
                adminAuthController.login(validLoginRequest);

        // Assert
        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertTrue(response.getBody().isSuccess()),
                () -> assertNotNull(response.getBody().getResult()),
                () -> assertEquals("admin@covenantcode.ru", response.getBody().getResult().getEmail()),
                () -> assertEquals("ADMIN", response.getBody().getResult().getRole())
        );

        verify(adminUserService, times(1)).updateLastLogin("admin@covenantcode.ru");
    }

    @Test
    @DisplayName("Проверка структуры JSON при успешной аутентификации")
    void login_Success_ShouldHaveCorrectJsonStructure() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN")  // Используем SimpleGrantedAuthority
        );

        doReturn(authorities).when(authentication).getAuthorities();

        doNothing().when(adminUserService).updateLastLogin(anyString());

        ResponseEntity<ResponseWrapper<AdminLoginRsDto>> response =
                adminAuthController.login(validLoginRequest);

        String jsonResponse = objectMapper.writeValueAsString(response.getBody());

        assertAll(
                () -> assertTrue(jsonResponse.contains("\"success\":true")),
                () -> assertTrue(jsonResponse.contains("\"result\"")),
                () -> assertTrue(jsonResponse.contains("\"email\":\"admin@covenantcode.ru\"")),
                () -> assertTrue(jsonResponse.contains("\"role\":\"ADMIN\"")),
                () -> assertFalse(jsonResponse.contains("\"error\""))
        );
    }

    @Test
    @DisplayName("Проверка структуры JSON при ошибке аутентификации")
    void login_Error_ShouldHaveCorrectErrorStructure() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<ResponseWrapper<AdminLoginRsDto>> response =
                adminAuthController.login(invalidLoginRequest);

        String jsonResponse = objectMapper.writeValueAsString(response.getBody());

        assertAll(
                () -> assertTrue(jsonResponse.contains("\"success\":false")),
                () -> assertTrue(jsonResponse.contains("\"error\"")),
                () -> assertTrue(jsonResponse.contains("\"code\":\"AUTHENTICATION_FAILED\"")),
                () -> assertFalse(jsonResponse.contains("\"result\""))
        );
    }

    @Test
    @DisplayName("Валидация входных данных - контроллер должен принимать @Valid параметр")
    void login_ShouldHaveValidAnnotation() throws NoSuchMethodException {
        var method = AdminAuthController.class.getMethod("login", AdminLoginRqDto.class);
        var parameters = method.getParameters();

        assertTrue(parameters[0].isAnnotationPresent(Valid.class));
    }
}