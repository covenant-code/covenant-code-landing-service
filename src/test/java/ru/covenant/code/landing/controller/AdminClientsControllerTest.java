package ru.covenant.code.landing.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.exceptions.ValidationException;
import ru.covenant.code.landing.service.client.ClientsService;
import ru.covenant.code.landing.entity.enumerated.Status;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты контроллера админки для работы с клиентами")
@Tag("unit")
class AdminClientsControllerTest {

    @Mock
    private ClientsService clientsService;

    @InjectMocks
    private AdminClientsController controller;

    private List<ClientsAdminRsDto> mockClients;
    private UUID testId1;
    private UUID testId2;

    @BeforeEach
    void setUp() {
        testId1 = UUID.randomUUID();
        testId2 = UUID.randomUUID();

        ClientsAdminRsDto client1 = new ClientsAdminRsDto();
        client1.setId(testId1);
        client1.setName("Иван Петров");
        client1.setStatus(Status.NEW);

        ClientsAdminRsDto client2 = new ClientsAdminRsDto();
        client2.setId(testId2);
        client2.setName("Анна Смирнова");
        client2.setStatus(Status.NEW);

        mockClients = List.of(client1, client2);
    }

    @Test
    @DisplayName("Успешное получение клиентов по статусу - должен вернуть 200 OK со списком DTO")
    void getClientsByStatus_ShouldReturnSuccessResponse() {
        String status = "NEW";
        when(clientsService.getClientsByStatus(status)).thenReturn(mockClients);

        ResponseWrapper<List<ClientsAdminRsDto>> response = controller.getClientsByStatus(status);

        assertTrue(response.isSuccess());
        assertNotNull(response.getResult());
        assertEquals(2, response.getResult().size());
        assertNull(response.getError());
        verify(clientsService, times(1)).getClientsByStatus(eq(status));
    }

    @Test
    @DisplayName("Тест регистронезависимости - статус new (нижний регистр)")
    void getClientsByStatus_WithLowerCaseStatus_ShouldWork() {
        String status = "new";
        when(clientsService.getClientsByStatus(status)).thenReturn(mockClients);

        ResponseWrapper<List<ClientsAdminRsDto>> response = controller.getClientsByStatus(status);

        assertTrue(response.isSuccess());
        verify(clientsService, times(1)).getClientsByStatus(eq(status));
    }

    @Test
    @DisplayName("Тест регистронезависимости - статус New (смешанный регистр)")
    void getClientsByStatus_WithMixedCaseStatus_ShouldWork() {
        String status = "New";
        when(clientsService.getClientsByStatus(status)).thenReturn(mockClients);

        ResponseWrapper<List<ClientsAdminRsDto>> response = controller.getClientsByStatus(status);

        assertTrue(response.isSuccess());
        verify(clientsService, times(1)).getClientsByStatus(eq(status));
    }

    @Test
    @DisplayName("Некорректный статус - должен пробрасывать ValidationException")
    void getClientsByStatus_WithInvalidStatus_ShouldThrowValidationException() {
        String invalidStatus = "INVALID_STATUS";
        when(clientsService.getClientsByStatus(invalidStatus))
                .thenThrow(new ValidationException("Некорректный статус: " + invalidStatus));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.getClientsByStatus(invalidStatus));

        assertTrue(exception.getMessage().contains("Некорректный статус: " + invalidStatus));
        verify(clientsService, times(1)).getClientsByStatus(eq(invalidStatus));
    }

    @Test
    @DisplayName("Пустой результат - должен возвращать пустой список")
    void getClientsByStatus_WithEmptyResult_ShouldReturnEmptyList() {
        String status = "DONE";
        when(clientsService.getClientsByStatus(status)).thenReturn(List.of());

        ResponseWrapper<List<ClientsAdminRsDto>> response = controller.getClientsByStatus(status);

        assertTrue(response.isSuccess());
        assertNotNull(response.getResult());
        assertTrue(response.getResult().isEmpty());
        verify(clientsService, times(1)).getClientsByStatus(eq(status));
    }
}