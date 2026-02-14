package ru.covenant.code.landing.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.assertj.core.api.InstanceOfAssertFactories;

import static org.assertj.core.api.Assertions.assertThat;

class AdminPermissionExceptionTest {

    @Test
    void constructorShouldSetCorrectFields() {
        String required = "ADMIN";
        String current = "USER";

        AdminPermissionException ex = new AdminPermissionException(required, current);

        assertThat(ex).isInstanceOf(BusinessException.class);
        assertThat(ex.getErrorCode()).isEqualTo("ADMIN_PERMISSION_DENIED");
        assertThat(ex.getDescription()).isEqualTo("Недостаточно прав");
        assertThat(ex.getMessage()).isEqualTo("Требуется роль 'ADMIN', текущая роль 'USER'");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(ex.getDetails())
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsEntry("requiredPermission", "ADMIN")
                .containsEntry("currentPermission", "USER");

        assertThat(ex.getRequiredPermission()).isEqualTo(required);
        assertThat(ex.getCurrentPermission()).isEqualTo(current);
    }
}