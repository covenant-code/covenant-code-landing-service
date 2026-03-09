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

    @InjectMocks
    private AdminAuthController adminAuthController;

    private ObjectMapper objectMapper;
    private AdminLoginRqDto validLoginRequest;
    private AdminLoginRqDto invalidLoginRequest;
    private AdminLoginRsDto successResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        validLoginRequest = new AdminLoginRqDto();
        validLoginRequest.setEmail("admin@covenantcode.ru");
        validLoginRequest.setPassword("admin123");

        invalidLoginRequest = new AdminLoginRqDto();
        invalidLoginRequest.setEmail("wrong@covenantcode.ru");
        invalidLoginRequest.setPassword("wrongpass");

        successResponse = AdminLoginRsDto.builder()
                .success(true)
                .message("Авторизация успешна")
                .authenticated(true)
                .email(validLoginRequest.getEmail())
                .role("ADMIN")
                .build();
    }

    @Test
    @DisplayName("Успешная аутентификация - должен вернуть 200 OK с данными пользователя и ролью")
    void login_Success_ShouldReturn200WithUserDataAndRole() {
        when(adminUserService.updateLastLogin(validLoginRequest)).thenReturn(successResponse);

        ResponseEntity<ResponseWrapper<AdminLoginRsDto>> response = adminAuthController.login(validLoginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getResult());
        assertEquals("ADMIN", response.getBody().getResult().getRole());
        assertEquals(validLoginRequest.getEmail(), response.getBody().getResult().getEmail());
        assertNull(response.getBody().getError());

        verify(adminUserService, times(1)).updateLastLogin(validLoginRequest);
        verifyNoInteractions(authenticationManager);
    }



    @Test
    @DisplayName("Неверные учетные данные - должен вернуть 401 UNAUTHORIZED с кодом AUTHENTICATION_FAILED")
    void login_BadCredentials_ShouldReturn401WithAuthenticationFailedCode() {

        when(adminUserService.updateLastLogin(invalidLoginRequest))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<ResponseWrapper<AdminLoginRsDto>> response = adminAuthController.login(invalidLoginRequest);

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

        verify(adminUserService, times(1)).updateLastLogin(invalidLoginRequest);
    }

    @Test
    @DisplayName("Заблокированный пользователь - должен вернуть 401 UNAUTHORIZED с сообщением о блокировке")
    void login_DisabledUser_ShouldReturn401WithBlockedMessage() {
        when(adminUserService.updateLastLogin(validLoginRequest))
                .thenThrow(new DisabledException("Учетная запись заблокирована"));

        ResponseEntity<ResponseWrapper<AdminLoginRsDto>> response =
                adminAuthController.login(validLoginRequest);

        assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertFalse(response.getBody().isSuccess()),
                () -> assertNull(response.getBody().getResult()),
                () -> assertNotNull(response.getBody().getError()),
                () -> assertEquals("AUTHENTICATION_ERROR", response.getBody().getError().getCode()),
                () -> assertEquals("Пользователя не существует", response.getBody().getError().getDescription()),
                () -> assertEquals("Учетная запись заблокирована", response.getBody().getError().getMessage())
        );

        verify(adminUserService, times(1)).updateLastLogin(validLoginRequest);
    }

    @Test
    @DisplayName("Неизвестная ошибка сервиса - должен вернуть 401 UNAUTHORIZED")
    void login_GenericException_ShouldReturn401() {
        String errorMessage = "Database connection error";
        when(adminUserService.updateLastLogin(validLoginRequest))
                .thenThrow(new RuntimeException(errorMessage));

        ResponseEntity<ResponseWrapper<AdminLoginRsDto>> response = adminAuthController.login(validLoginRequest);

        assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertFalse(response.getBody().isSuccess()),
                () -> assertNull(response.getBody().getResult()),
                () -> assertNotNull(response.getBody().getError()),
                () -> assertEquals("AUTHENTICATION_ERROR", response.getBody().getError().getCode()),
                () -> assertEquals("Пользователя не существует", response.getBody().getError().getDescription()),
                () -> assertEquals(errorMessage, response.getBody().getError().getMessage())
        );

        verify(adminUserService, times(1)).updateLastLogin(validLoginRequest);
    }

    @Test
    @DisplayName("Проверка структуры JSON при ошибке аутентификации")
    void login_Error_ShouldHaveCorrectErrorStructure() throws Exception {
        when(adminUserService.updateLastLogin(invalidLoginRequest))
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