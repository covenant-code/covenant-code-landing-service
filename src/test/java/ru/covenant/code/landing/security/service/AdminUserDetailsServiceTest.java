package ru.covenant.code.landing.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.covenant.code.landing.entity.AdminUser;
import ru.covenant.code.landing.entity.enumerated.AdminRole;
import ru.covenant.code.landing.repository.AdminUserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса загрузки пользователей AdminUserDetailsService")
@Tag("unit")
class AdminUserDetailsServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @InjectMocks
    private AdminUserDetailsService adminUserDetailsService;

    private AdminUser activeUser;
    private AdminUser inactiveUser;
    private final String testEmail = "admin@covenantcode.ru";
    private final String testPassword = "encodedPassword123";

    @BeforeEach
    void setUp() {
        activeUser = AdminUser.builder()
                .id(UUID.randomUUID())
                .email(testEmail)
                .password(testPassword)
                .firstName("Иван")
                .lastName("Иванов")
                .role(AdminRole.ADMIN)
                .active(true)
                .build();

        inactiveUser = AdminUser.builder()
                .id(UUID.randomUUID())
                .email(testEmail)
                .password(testPassword)
                .firstName("Петр")
                .lastName("Петров")
                .role(AdminRole.MODERATOR)
                .active(false)
                .build();
    }

    @Test
    @DisplayName("Успешная загрузка активного пользователя - должен вернуть UserDetails с правильной ролью")
    void loadUserByUsername_Success_ShouldReturnUserDetailsWithCorrectRole() {
        when(adminUserRepository.findByEmail(testEmail))
                .thenReturn(Optional.of(activeUser));

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername(testEmail);

        assertAll(
                () -> assertNotNull(userDetails),
                () -> assertEquals(testEmail, userDetails.getUsername()),
                () -> assertEquals(testPassword, userDetails.getPassword()),
                () -> assertTrue(userDetails.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))),
                () -> assertEquals(1, userDetails.getAuthorities().size())
        );

        verify(adminUserRepository, times(1)).findByEmail(testEmail);
    }

    @Test
    @DisplayName("Успешная загрузка пользователя с ролью SUPER_ADMIN - должен вернуть ROLE_SUPER_ADMIN")
    void loadUserByUsername_WithSuperAdminRole_ShouldReturnCorrectRole() {
        AdminUser superAdmin = AdminUser.builder()
                .id(UUID.randomUUID())
                .email("super@covenantcode.ru")
                .password(testPassword)
                .firstName("Сергей")
                .lastName("Сергеев")
                .role(AdminRole.SUPER_ADMIN)
                .active(true)
                .build();

        when(adminUserRepository.findByEmail("super@covenantcode.ru"))
                .thenReturn(Optional.of(superAdmin));

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername("super@covenantcode.ru");

        assertAll(
                () -> assertNotNull(userDetails),
                () -> assertTrue(userDetails.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN"))),
                () -> assertEquals(1, userDetails.getAuthorities().size())
        );
    }

    @Test
    @DisplayName("Успешная загрузка пользователя с ролью MODERATOR - должен вернуть ROLE_MODERATOR")
    void loadUserByUsername_WithModeratorRole_ShouldReturnCorrectRole() {
        AdminUser moderator = AdminUser.builder()
                .id(UUID.randomUUID())
                .email("moderator@covenantcode.ru")
                .password(testPassword)
                .firstName("Анна")
                .lastName("Смирнова")
                .role(AdminRole.MODERATOR)
                .active(true)
                .build();

        when(adminUserRepository.findByEmail("moderator@covenantcode.ru"))
                .thenReturn(Optional.of(moderator));

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername("moderator@covenantcode.ru");

        assertAll(
                () -> assertNotNull(userDetails),
                () -> assertTrue(userDetails.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_MODERATOR"))),
                () -> assertEquals(1, userDetails.getAuthorities().size())
        );
    }

    @Test
    @DisplayName("Успешная загрузка пользователя с ролью SUPPORT - должен вернуть ROLE_SUPPORT")
    void loadUserByUsername_WithSupportRole_ShouldReturnCorrectRole() {
        AdminUser support = AdminUser.builder()
                .id(UUID.randomUUID())
                .email("support@covenantcode.ru")
                .password(testPassword)
                .firstName("Ольга")
                .lastName("Козлова")
                .role(AdminRole.SUPPORT)
                .active(true)
                .build();

        when(adminUserRepository.findByEmail("support@covenantcode.ru"))
                .thenReturn(Optional.of(support));

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername("support@covenantcode.ru");

        assertAll(
                () -> assertNotNull(userDetails),
                () -> assertTrue(userDetails.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPPORT"))),
                () -> assertEquals(1, userDetails.getAuthorities().size())
        );
    }

    @Test
    @DisplayName("Загрузка неактивного пользователя - должен выбросить DisabledException")
    void loadUserByUsername_InactiveUser_ShouldThrowDisabledException() {
        when(adminUserRepository.findByEmail(testEmail))
                .thenReturn(Optional.of(inactiveUser));

        DisabledException exception = assertThrows(
                DisabledException.class,
                () -> adminUserDetailsService.loadUserByUsername(testEmail)
        );

        assertEquals("Учетная запись заблокирована", exception.getMessage());
        verify(adminUserRepository, times(1)).findByEmail(testEmail);
    }

    @Test
    @DisplayName("Загрузка несуществующего пользователя - должен выбросить UsernameNotFoundException")
    void loadUserByUsername_UserNotFound_ShouldThrowUsernameNotFoundException() {
        String nonExistentEmail = "nonexistent@covenantcode.ru";
        when(adminUserRepository.findByEmail(nonExistentEmail))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> adminUserDetailsService.loadUserByUsername(nonExistentEmail)
        );

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(adminUserRepository, times(1)).findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("Загрузка с null email - должен выбросить исключение (зависит от реализации)")
    void loadUserByUsername_NullEmail_ShouldThrowException() {
        assertThrows(Exception.class, () -> adminUserDetailsService.loadUserByUsername(null));
    }

    @Test
    @DisplayName("Загрузка с пустым email - должен выбросить исключение (зависит от реализации)")
    void loadUserByUsername_EmptyEmail_ShouldThrowException() {
        assertThrows(Exception.class, () -> adminUserDetailsService.loadUserByUsername(""));
    }

    @Test
    @DisplayName("Проверка что пароль не изменяется при загрузке")
    void loadUserByUsername_ShouldPreservePassword() {
        when(adminUserRepository.findByEmail(testEmail))
                .thenReturn(Optional.of(activeUser));

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername(testEmail);

        assertEquals(testPassword, userDetails.getPassword());
        assertNotEquals("wrongPassword", userDetails.getPassword());
    }

    @Test
    @DisplayName("Проверка что username равен email")
    void loadUserByUsername_UsernameShouldBeEmail() {
        when(adminUserRepository.findByEmail(testEmail))
                .thenReturn(Optional.of(activeUser));

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername(testEmail);

        assertEquals(testEmail, userDetails.getUsername());
        assertNotEquals("different@email.ru", userDetails.getUsername());
    }

    @Test
    @DisplayName("Проверка всех ролей из enum AdminRole")
    void loadUserByUsername_AllRoles_ShouldWorkCorrectly() {
        for (AdminRole role : AdminRole.values()) {
            AdminUser user = AdminUser.builder()
                    .id(UUID.randomUUID())
                    .email(role.name().toLowerCase() + "@covenantcode.ru")
                    .password(testPassword)
                    .firstName("Test")
                    .lastName("User")
                    .role(role)
                    .active(true)
                    .build();

            when(adminUserRepository.findByEmail(user.getEmail()))
                    .thenReturn(Optional.of(user));

            UserDetails userDetails = adminUserDetailsService.loadUserByUsername(user.getEmail());

            assertAll(
                    () -> assertNotNull(userDetails),
                    () -> assertTrue(userDetails.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role.name()))),
                    () -> assertEquals(1, userDetails.getAuthorities().size())
            );
        }
    }
}