package ru.covenant.code.landing.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.exceptions.ClientNotFoundException;
import ru.covenant.code.landing.exceptions.PersistenceException;
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
    private UUID testUpdateId;
    private ClientsUpdateRqDto validUpdateDto;
    private ClientsAdminRsDto updatedClientDto;

    @BeforeEach
    void setUp() {
        testId1 = UUID.randomUUID();
        testId2 = UUID.randomUUID();
        testUpdateId = UUID.randomUUID();

        ClientsAdminRsDto client1 = new ClientsAdminRsDto();
        client1.setId(testId1);
        client1.setName("Иван Петров");
        client1.setStatus(Status.NEW);

        ClientsAdminRsDto client2 = new ClientsAdminRsDto();
        client2.setId(testId2);
        client2.setName("Анна Смирнова");
        client2.setStatus(Status.NEW);

        mockClients = List.of(client1, client2);

        validUpdateDto = ClientsUpdateRqDto.builder()
                .name("Иван Иванов")
                .email("ivan.ivanov@example.com")
                .phone("+79161234567")
                .message("Обновленное сообщение")
                .courseType("BACKEND")
                .status("PROCESSED")
                .priority("HIGH")
                .source("Лендинг")
                .processedBy("admin@covenantcode.ru")
                .build();

        updatedClientDto = new ClientsAdminRsDto();
        updatedClientDto.setId(testUpdateId);
        updatedClientDto.setName("Иван Иванов");
        updatedClientDto.setEmail("ivan.ivanov@example.com");
        updatedClientDto.setPhone("+79161234567");
        updatedClientDto.setMessage("Обновленное сообщение");
        updatedClientDto.setCourseType(CourseType.BACKEND);
        updatedClientDto.setStatus(Status.PROCESSED);
        updatedClientDto.setPriority(Priority.HIGH);
        updatedClientDto.setSource("Лендинг");
        updatedClientDto.setProcessedBy("admin@covenantcode.ru");
        updatedClientDto.setStatusLabel("В обработке");
        updatedClientDto.setPriorityLabel("Высокий");
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

    @Test
    @DisplayName("Успешное обновление клиента - должен вернуть 200 OK с обновленным DTO")
    void updateClient_ShouldReturnSuccessResponse() {
        when(clientsService.updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class)))
                .thenReturn(updatedClientDto);

        ResponseWrapper<ClientsAdminRsDto> response = controller.updateClient(testUpdateId, validUpdateDto);

        assertTrue(response.isSuccess());
        assertNotNull(response.getResult());
        assertEquals(testUpdateId, response.getResult().getId());
        assertEquals("Иван Иванов", response.getResult().getName());
        assertEquals("ivan.ivanov@example.com", response.getResult().getEmail());
        assertEquals(CourseType.BACKEND, response.getResult().getCourseType());
        assertEquals(Status.PROCESSED, response.getResult().getStatus());
        assertEquals(Priority.HIGH, response.getResult().getPriority());
        assertEquals("В обработке", response.getResult().getStatusLabel());
        assertEquals("Высокий", response.getResult().getPriorityLabel());
        assertNull(response.getError());

        verify(clientsService, times(1)).updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class));
    }

    @Test
    @DisplayName("Частичное обновление - только статус и приоритет")
    void updateClient_WithPartialData_ShouldUpdateOnlySpecifiedFields() {
        ClientsUpdateRqDto partialUpdateDto = ClientsUpdateRqDto.builder()
                .status("DONE")
                .priority("LOW")
                .build();

        ClientsAdminRsDto partiallyUpdatedDto = new ClientsAdminRsDto();
        partiallyUpdatedDto.setId(testUpdateId);
        partiallyUpdatedDto.setName("Иван Петров"); // сохранилось старое имя
        partiallyUpdatedDto.setStatus(Status.DONE);
        partiallyUpdatedDto.setPriority(Priority.LOW);
        partiallyUpdatedDto.setStatusLabel("Обработано");
        partiallyUpdatedDto.setPriorityLabel("Низкий");

        when(clientsService.updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class)))
                .thenReturn(partiallyUpdatedDto);

        ResponseWrapper<ClientsAdminRsDto> response = controller.updateClient(testUpdateId, partialUpdateDto);

        assertTrue(response.isSuccess());
        assertNotNull(response.getResult());
        assertEquals(Status.DONE, response.getResult().getStatus());
        assertEquals(Priority.LOW, response.getResult().getPriority());
        assertEquals("Обработано", response.getResult().getStatusLabel());
        assertEquals("Низкий", response.getResult().getPriorityLabel());

        verify(clientsService, times(1)).updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class));
    }

    @Test
    @DisplayName("Клиент не найден - должен пробрасывать ClientNotFoundException")
    void updateClient_WhenClientNotFound_ShouldThrowClientNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();
        when(clientsService.updateClient(eq(nonExistentId), any(ClientsUpdateRqDto.class)))
                .thenThrow(new ClientNotFoundException(nonExistentId));

        ClientNotFoundException exception = assertThrows(ClientNotFoundException.class,
                () -> controller.updateClient(nonExistentId, validUpdateDto));

        assertTrue(exception.getMessage().contains("не найдена"));
        assertEquals(nonExistentId, exception.getClientId());
        verify(clientsService, times(1)).updateClient(eq(nonExistentId), any(ClientsUpdateRqDto.class));
    }

    @Test
    @DisplayName("Ошибка сохранения в БД - должен пробрасывать PersistenceException")
    void updateClient_WhenPersistenceError_ShouldThrowPersistenceException() {
        when(clientsService.updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class)))
                .thenThrow(PersistenceException.update("Clients", new RuntimeException("DB error")));

        PersistenceException exception = assertThrows(PersistenceException.class,
                () -> controller.updateClient(testUpdateId, validUpdateDto));

        assertTrue(exception.getMessage().contains("Clients"));
        assertTrue(exception.getMessage().contains("обновление"));
        verify(clientsService, times(1)).updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class));
    }

    @Test
    @DisplayName("Валидация - пустое имя должно вызывать ValidationException")
    void updateClient_WithEmptyName_ShouldThrowValidationException() {
        ClientsUpdateRqDto invalidDto = ClientsUpdateRqDto.builder()
                .name("")  // пустое имя
                .email("valid@example.com")
                .build();

        // Имитируем валидацию на уровне сервиса
        when(clientsService.updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class)))
                .thenThrow(new ValidationException("Имя обязательно"));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.updateClient(testUpdateId, invalidDto));

        assertTrue(exception.getMessage().contains("Имя обязательно"));
        verify(clientsService, times(1)).updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class));
    }

    @Test
    @DisplayName("Валидация - невалидный email должен вызывать ValidationException")
    void updateClient_WithInvalidEmail_ShouldThrowValidationException() {
        ClientsUpdateRqDto invalidDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("invalid-email")  // невалидный email
                .build();

        when(clientsService.updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class)))
                .thenThrow(new ValidationException("Неверный формат email"));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.updateClient(testUpdateId, invalidDto));

        assertTrue(exception.getMessage().contains("Неверный формат email"));
        verify(clientsService, times(1)).updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class));
    }

    @Test
    @DisplayName("Валидация - невалидный тип курса должен вызывать ValidationException")
    void updateClient_WithInvalidCourseType_ShouldThrowValidationException() {
        ClientsUpdateRqDto invalidDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .courseType("INVALID_COURSE")  // невалидный курс
                .build();

        when(clientsService.updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class)))
                .thenThrow(new ValidationException("Некорректный тип курса: INVALID_COURSE"));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.updateClient(testUpdateId, invalidDto));

        assertTrue(exception.getMessage().contains("Некорректный тип курса"));
        verify(clientsService, times(1)).updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class));
    }

    @Test
    @DisplayName("Валидация - невалидный статус должен вызывать ValidationException")
    void updateClient_WithInvalidStatus_ShouldThrowValidationException() {
        ClientsUpdateRqDto invalidDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .status("INVALID_STATUS")  // невалидный статус
                .build();

        when(clientsService.updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class)))
                .thenThrow(new ValidationException("Некорректный статус: INVALID_STATUS"));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.updateClient(testUpdateId, invalidDto));

        assertTrue(exception.getMessage().contains("Некорректный статус"));
        verify(clientsService, times(1)).updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class));
    }

    @Test
    @DisplayName("Валидация - невалидный приоритет должен вызывать ValidationException")
    void updateClient_WithInvalidPriority_ShouldThrowValidationException() {
        ClientsUpdateRqDto invalidDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .priority("INVALID_PRIORITY")  // невалидный приоритет
                .build();

        when(clientsService.updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class)))
                .thenThrow(new ValidationException("Некорректный приоритет: INVALID_PRIORITY"));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.updateClient(testUpdateId, invalidDto));

        assertTrue(exception.getMessage().contains("Некорректный приоритет"));
        verify(clientsService, times(1)).updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class));
    }

    @Test
    @DisplayName("Валидация - невалидный телефон должен вызывать ValidationException")
    void updateClient_WithInvalidPhone_ShouldThrowValidationException() {
        ClientsUpdateRqDto invalidDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .phone("123456")  // невалидный телефон
                .build();

        when(clientsService.updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class)))
                .thenThrow(new ValidationException("Телефон должен быть в формате +7XXXXXXXXXX"));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.updateClient(testUpdateId, invalidDto));

        assertTrue(exception.getMessage().contains("Телефон должен быть в формате"));
        verify(clientsService, times(1)).updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class));
    }

    @Test
    @DisplayName("Проверка структуры ResponseWrapper - успешный ответ")
    void updateClient_ShouldReturnProperResponseWrapperStructure() {
        when(clientsService.updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class)))
                .thenReturn(updatedClientDto);

        ResponseWrapper<ClientsAdminRsDto> response = controller.updateClient(testUpdateId, validUpdateDto);

        assertTrue(response.isSuccess());
        assertNotNull(response.getResult());
        assertNull(response.getError());
    }

    @Test
    @DisplayName("Проверка вызова сервиса с правильными параметрами")
    void updateClient_ShouldCallServiceWithCorrectParameters() {
        when(clientsService.updateClient(eq(testUpdateId), any(ClientsUpdateRqDto.class)))
                .thenReturn(updatedClientDto);

        controller.updateClient(testUpdateId, validUpdateDto);

        verify(clientsService, times(1)).updateClient(eq(testUpdateId), eq(validUpdateDto));
    }

}