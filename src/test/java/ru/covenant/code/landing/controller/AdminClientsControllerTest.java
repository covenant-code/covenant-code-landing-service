package ru.covenant.code.landing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.error.ResponseWrapper;
import ru.covenant.code.landing.exceptions.ClientNotFoundException;
import ru.covenant.code.landing.exceptions.PersistenceException;
import ru.covenant.code.landing.exceptions.ValidationException;
import ru.covenant.code.landing.service.client.ClientsService;


import java.util.List;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для контроллера AdminClientsController для работы с клиентами")
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

    private MockMvc mockMvc;

    @InjectMocks
    private AdminClientsController adminClientsController;

    private ObjectMapper mapper;
    private UUID testUuid;
    private UUID notFoundClientId;
    private OffsetDateTime now;

    private ClientNotFoundException clientNotFoundException;
    private ClientsAdminRsDto testClientAdminRsDto;

    String expectedErrorMessage;


    private ResponseWrapper<ClientsAdminRsDto> testResponseWrapper;

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

        notFoundClientId = UUID.randomUUID();
        clientNotFoundException = new ClientNotFoundException(notFoundClientId);

        mockMvc = MockMvcBuilders.standaloneSetup(adminClientsController).build();
        mapper = new ObjectMapper();
        now = OffsetDateTime.now();

        testUuid = UUID.fromString("7da674d5-0672-4d0b-a7d3-8f4ee5d3a434");
        notFoundClientId = UUID.fromString("999e4567-e89b-12d3-a456-426614174999");

        expectedErrorMessage = "Заявка с ID {" + notFoundClientId + "} не найдена";

        testClientAdminRsDto = new ClientsAdminRsDto();
        testClientAdminRsDto.setId(testUuid);
        testClientAdminRsDto.setName("Иван Петров");
        testClientAdminRsDto.setEmail("ivan.petrov@example.com");
        testClientAdminRsDto.setPhone("+79001234567");
        testClientAdminRsDto.setMessage("Хочу записаться на курс по Java-разработке. Интересует подробная программа и стоимость.");
        testClientAdminRsDto.setCourseType(CourseType.BACKEND);
        testClientAdminRsDto.setStatus(Status.NEW);
        testClientAdminRsDto.setPriority(Priority.MEDIUM);
        testClientAdminRsDto.setStatusLabel("Новые");
        testClientAdminRsDto.setPriorityLabel("средний");
        testClientAdminRsDto.setSource("Лендинг");
        testClientAdminRsDto.setCreatedAt("CURRENT_TIMESTAMP");
        testClientAdminRsDto.setUpdatedAt("CURRENT_TIMESTAMP");
        testClientAdminRsDto.setProcessedBy(null);
        testClientAdminRsDto.setProcessedAt(null);
        testClientAdminRsDto.setFormattedCreatedAt("12 мар. 2026г., 17:03");
        testClientAdminRsDto.setFormattedUpdatedAt("12 мар. 2026г., 17:03");
        testClientAdminRsDto.setFormattedProcessedAt(null);

        testResponseWrapper = ResponseWrapper.success(testClientAdminRsDto);
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
    @DisplayName("Тест 1: Мок сервиса с возвратом DTO")
    void getClientById_ShouldReturnClientsAdminRsDto_WhenServiceReturnsDto() throws Exception {
        when(clientsService.getClientById(testUuid)).thenReturn(testClientAdminRsDto);

        mockMvc.perform(get("/api/v1/admin/clients/{id}", testUuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())

                .andExpect(jsonPath("$.result.id").value(testUuid.toString()))
                .andExpect(jsonPath("$.result.name").value("Иван Петров"))
                .andExpect(jsonPath("$.result.email").value("ivan.petrov@example.com"))
                .andExpect(jsonPath("$.result.phone").value("+79001234567"))
                .andExpect(jsonPath("$.result.message").value("Хочу записаться на курс по Java-разработке. Интересует подробная программа и стоимость."))
                .andExpect(jsonPath("$.result.courseType").value("BACKEND"))
                .andExpect(jsonPath("$.result.status").value("NEW"))
                .andExpect(jsonPath("$.result.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.result.source").value("Лендинг"))
                .andExpect(jsonPath("$.result.createdAt").value("CURRENT_TIMESTAMP"))
                .andExpect(jsonPath("$.result.updatedAt").value("CURRENT_TIMESTAMP"));


        verify(clientsService, times(1)).getClientById(testUuid);
    }

    @Test
    @DisplayName("Тест 2: Проверка HTTP 200 при успешном запросе")
    void getClientById_ShouldReturnHttp200_WhenClientExists() throws Exception {
        when(clientsService.getClientById(testUuid)).thenReturn(testClientAdminRsDto);


        mockMvc.perform(get("/api/v1/admin/clients/{id}", testUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.id").exists())
                .andExpect(jsonPath("$.result.name").isString());

        verify(clientsService, times(1)).getClientById(testUuid);
    }

    @Test
    @DisplayName("Тест 3: Проверка вызова сервиса с правильным UUID")
    void getClientById_ShouldCallServiceWithCorrectUuid() throws Exception {
        when(clientsService.getClientById(testUuid)).thenReturn(testClientAdminRsDto);

        mockMvc.perform(get("/api/v1/admin/clients/{id}", testUuid));

        verify(clientsService, times(1)).getClientById(testUuid);

        UUID otherUuid = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
        verify(clientsService, never()).getClientById(otherUuid);

        verify(clientsService, never()).getClientById(UUID.randomUUID());

        verify(clientsService).getClientById(argThat(uuid ->
                uuid.equals(testUuid)));
    }

    @Test
    @DisplayName("Тест 4: Мок сервиса с ClientNotFoundException")
    void getClientById_WhenClientNotFound_ShouldThrowClientNotFoundException() throws Exception {

        assertNotNull(clientNotFoundException, "Объект исключения должен быть инициализирован");

        when(clientsService.getClientById(notFoundClientId)).thenThrow(clientNotFoundException);

        ClientNotFoundException thrown = assertThrows(ClientNotFoundException.class, () -> {
            clientsService.getClientById(notFoundClientId);
        });
        verify(clientsService, times(1)).getClientById(notFoundClientId);
    }

    @Test
    @DisplayName("Тест 5: Проверка валидации UUID формата")
    void getClientById_ShouldAcceptOnlyValidUuidFormat() throws Exception {

        when(clientsService.getClientById(testUuid)).thenReturn(testClientAdminRsDto);

        mockMvc.perform(get("/api/v1/admin/clients/{id}", testUuid.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(testUuid.toString()));

        verify(clientsService, times(1)).getClientById(testUuid);
    }

    @Test
    @DisplayName("Тест 6: Проверка что @PathVariable правильно передает UUID в метод")
    void getClientById_ShouldPassPathVariableToService() throws Exception {

        when(clientsService.getClientById(testUuid)).thenReturn(testClientAdminRsDto);


        ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);

        mockMvc.perform(get("/api/v1/admin/clients/{id}", testUuid.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(clientsService, times(1)).getClientById(uuidCaptor.capture());

        UUID capturedUuid = uuidCaptor.getValue();

        assertNotNull(capturedUuid, "UUID не должен быть null");
        assertEquals(testUuid, capturedUuid,
                "UUID из @PathVariable должен совпадать с переданным в запросе");
        assertEquals(testUuid.toString(), capturedUuid.toString(),
                "Строковое представление UUID должно совпадать");
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
        partiallyUpdatedDto.setName("Иван Петров");
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
                .email("invalid-email")
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
                .courseType("INVALID_COURSE")
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
                .status("INVALID_STATUS")
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
                .priority("INVALID_PRIORITY")  //
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
                .phone("123456")
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