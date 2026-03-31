package ru.covenant.code.landing.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.covenant.code.landing.entity.enumerated.AdminPermission;
import ru.covenant.code.landing.entity.enumerated.AdminRole;

import static org.junit.jupiter.api.Assertions.*;


class AdminUserTest {

    @Test
    @DisplayName("Проверка значений по умолчанию")
    void defaultValuesTest() {
        AdminUser admin = AdminUser.builder()
                .email("test@test.com")
                .password("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .build();

        assertEquals(AdminRole.ADMIN, admin.getRole());
        assertTrue(admin.isActive());
        assertNull(admin.getCreatedAt());

        admin.setRole(AdminRole.SUPER_ADMIN);
        admin.setActive(false);

        assertEquals(AdminRole.SUPER_ADMIN, admin.getRole());
        assertFalse(admin.isActive());

        AdminPermission permission = AdminPermission.CLIENT_VIEW;
        assertEquals("Просмотр клиентов", permission.getDescription());
    }
}