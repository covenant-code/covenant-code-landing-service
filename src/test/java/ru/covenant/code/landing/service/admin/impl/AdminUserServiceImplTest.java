package ru.covenant.code.landing.service.admin.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.covenant.code.landing.dto.admin.request.AdminLoginRqDto;
import ru.covenant.code.landing.dto.admin.response.AdminLoginRsDto;
import ru.covenant.code.landing.entity.AdminUser;
import ru.covenant.code.landing.repository.AdminUserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса администраторов")
@Tag("unit")
class AdminUserServiceImplTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private AdminLoginRqDto loginRequest;
    private AdminUser adminUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        loginRequest = new AdminLoginRqDto();
        loginRequest.setEmail("admin@covenantcode.ru");
        loginRequest.setPassword("admin123");

        adminUser = new AdminUser();
        adminUser.setId(userId);
        adminUser.setEmail("admin@covenantcode.ru");
        adminUser.setLastLoginAt(null);
    }

    @Test
    @DisplayName("updateLastLogin - успешное обновление времени последнего входа")
    void updateLastLogin_Success_ShouldUpdateLastLoginAndSaveUser() {
        when(adminUserRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(adminUser));

        LocalDateTime beforeTest = LocalDateTime.now();

        AdminLoginRsDto result = adminUserService.updateLastLogin(loginRequest);

        assertNotNull(result);

        ArgumentCaptor<AdminUser> userCaptor = ArgumentCaptor.forClass(AdminUser.class);
        verify(adminUserRepository).save(userCaptor.capture());
        AdminUser savedUser = userCaptor.getValue();

        assertNotNull(savedUser.getLastLoginAt());
        assertTrue(savedUser.getLastLoginAt().isAfter(beforeTest) ||
                savedUser.getLastLoginAt().isEqual(beforeTest));
        assertTrue(savedUser.getLastLoginAt().isBefore(LocalDateTime.now().plusSeconds(1)));

        verify(adminUserRepository, times(1)).save(any(AdminUser.class));
        verify(adminUserRepository, times(1)).findByEmail(loginRequest.getEmail());
    }

    @Test
    @DisplayName("updateLastLogin - несуществующий пользователь, метод не падает с исключением и возвращает пустой DTO")
    void updateLastLogin_UserNotFound_ShouldNotThrowExceptionAndReturnEmptyDto() {
        when(adminUserRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.empty());

        AdminLoginRsDto result = adminUserService.updateLastLogin(loginRequest);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNull(result.getMessage());
        assertFalse(result.isAuthenticated());
        assertNull(result.getEmail());
        assertNull(result.getRole());

        verify(adminUserRepository, never()).save(any());
        verify(adminUserRepository, times(1)).findByEmail(loginRequest.getEmail());
    }
}