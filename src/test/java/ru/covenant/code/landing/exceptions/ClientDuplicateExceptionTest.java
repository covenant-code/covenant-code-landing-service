package ru.covenant.code.landing.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.assertj.core.api.InstanceOfAssertFactories;

import static org.assertj.core.api.Assertions.assertThat;

class ClientDuplicateExceptionTest {

    @Test
    void constructorShouldSetCorrectFields() {
        String email = "test@test.com";
        String phone = "+79991112233";

        ClientDuplicateException ex = new ClientDuplicateException(email, phone);

        assertThat(ex).isInstanceOf(BusinessException.class);
        assertThat(ex.getErrorCode()).isEqualTo("CLIENT_DUPLICATE");
        assertThat(ex.getDescription()).isEqualTo("Дубликат заявки");
        assertThat(ex.getMessage()).isEqualTo("Заявка с email 'test@test.com' или телефоном '+79991112233' уже существует");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);

        assertThat(ex.getDetails())
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsEntry("email", "test@test.com")
                .containsEntry("phone", "+79991112233");

        assertThat(ex.email).isEqualTo(email);
        assertThat(ex.phone).isEqualTo(phone);
    }
}