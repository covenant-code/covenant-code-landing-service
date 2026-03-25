package ru.covenant.code.landing.service.client.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.dto.client.response.ClientsStatsRsDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.exceptions.BusinessException;
import ru.covenant.code.landing.exceptions.ClientNotFoundException;
import ru.covenant.code.landing.exceptions.PersistenceException;
import ru.covenant.code.landing.exceptions.ValidationException;
import ru.covenant.code.landing.mapper.ClientsMapper;
import ru.covenant.code.landing.repository.ClientsRepository;
import ru.covenant.code.landing.specification.ClientsSpecification;
import ru.covenant.code.landing.ws.service.WebSocketPublisher;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientsServiceImplTest {

    @Mock
    private ClientsRepository clientsRepository;
    @Mock
    private ClientsSpecification clientsSpecification;
    @Mock
    private ClientsMapper clientsMapper;
    @Mock
    private WebSocketPublisher webSocketPublisher;

    @InjectMocks
    private ClientsServiceImpl clientsService;

    private UUID testClientId;
    private Clients existingClient;
    private ClientsUpdateRqDto fullUpdateDto;
    private ClientsUpdateRqDto partialUpdateDto;
    private Clients updatedClient;
    private ClientsAdminRsDto responseDto;
    private ClientsStatsRsDto statsDto;

    @BeforeEach
    void setUp() {
        testClientId = UUID.randomUUID();

        existingClient = new Clients();
        existingClient.setId(testClientId);
        existingClient.setName("Иван Петров");
        existingClient.setEmail("ivan@example.com");
        existingClient.setPhone("+79161234567");
        existingClient.setMessage("Старое сообщение");
        existingClient.setCourseType(CourseType.BACKEND);
        existingClient.setStatus(Status.NEW);
        existingClient.setPriority(Priority.MEDIUM);
        existingClient.setSource("Лендинг");
        existingClient.setProcessedBy(null);
        existingClient.setProcessedAt(null);

        fullUpdateDto = ClientsUpdateRqDto.builder()
                .name("Иван Иванов")
                .email("ivan.ivanov@example.com")
                .phone("+79998887766")
                .message("Новое сообщение")
                .courseType("FRONTEND")
                .status("PROCESSED")
                .priority("HIGH")
                .source("Телефон")
                .processedBy("admin@covenantcode.ru")
                .build();

        partialUpdateDto = ClientsUpdateRqDto.builder()
                .status("DONE")
                .processedBy("admin@covenantcode.ru")
                .build();

        updatedClient = new Clients();
        updatedClient.setId(testClientId);
        updatedClient.setName("Иван Иванов");
        updatedClient.setEmail("ivan.ivanov@example.com");
        updatedClient.setPhone("+79998887766");
        updatedClient.setMessage("Новое сообщение");
        updatedClient.setCourseType(CourseType.FRONTEND);
        updatedClient.setStatus(Status.PROCESSED);
        updatedClient.setPriority(Priority.HIGH);
        updatedClient.setSource("Телефон");
        updatedClient.setProcessedBy("admin@covenantcode.ru");
        updatedClient.setProcessedAt(OffsetDateTime.now());

        responseDto = new ClientsAdminRsDto();
        responseDto.setId(testClientId);
        responseDto.setName("Иван Иванов");
        responseDto.setStatus(Status.PROCESSED);
        responseDto.setStatusLabel("В обработке");

        statsDto = new ClientsStatsRsDto();
        statsDto.setTotal(100);
        statsDto.setNewCount(10);
        statsDto.setProcessedCount(20);
        statsDto.setDoneCount(70);
    }

    @Test
    void getAllClients_WithValidFilter_ShouldReturnMappedClients() {
        // Given
        ClientsFilterRqDto filter = new ClientsFilterRqDto();
        Specification<Clients> specification = mock(Specification.class);
        List<Clients> clients = List.of(new Clients(), new Clients());
        List<ClientsAdminRsDto> expectedDtos = List.of(new ClientsAdminRsDto(), new ClientsAdminRsDto());

        when(clientsSpecification.withFilter(filter)).thenReturn(specification);
        when(clientsRepository.findAll(eq(specification), any(Sort.class))).thenReturn(clients);
        when(clientsMapper.toAdminResponseList(clients)).thenReturn(expectedDtos);

        // When
        List<ClientsAdminRsDto> result = clientsService.getAllClients(filter);

        // Then
        assertThat(result).isEqualTo(expectedDtos);
        assertThat(result).hasSize(2);

        verify(clientsSpecification).withFilter(filter);
        verify(clientsRepository).findAll(eq(specification), any(Sort.class));
        verify(clientsMapper).toAdminResponseList(clients);
    }

    @Test
    void getAllClients_WhenRepositoryThrowsException_ShouldThrowBusinessException() {
        // Given
        ClientsFilterRqDto filter = new ClientsFilterRqDto();
        Specification<Clients> specification = mock(Specification.class);

        when(clientsSpecification.withFilter(filter)).thenReturn(specification);
        when(clientsRepository.findAll(eq(specification), any(Sort.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When/Then
        assertThatThrownBy(() -> clientsService.getAllClients(filter))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ошибка при получении списка клиентов");

        verify(clientsSpecification).withFilter(filter);
        verify(clientsRepository).findAll(eq(specification), any(Sort.class));
        verifyNoInteractions(clientsMapper);
    }

    @Test
    @DisplayName("getClientsByStatus - успешное получение клиентов по статусу NEW")
    void getClientsByStatus_Success_ShouldReturnListOfDtos() {
        String statusInput = "new";
        Status expectedStatus = Status.NEW;

        List<Clients> mockClients = List.of(new Clients(), new Clients());
        List<ClientsAdminRsDto> expectedDtos = List.of(new ClientsAdminRsDto(), new ClientsAdminRsDto());

        when(clientsRepository.findByStatus(eq(expectedStatus), any(Sort.class)))
                .thenReturn(mockClients);
        when(clientsMapper.toAdminResponseList(mockClients))
                .thenReturn(expectedDtos);

        List<ClientsAdminRsDto> result = clientsService.getClientsByStatus(statusInput);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(clientsRepository, times(1)).findByStatus(eq(expectedStatus), any(Sort.class));
        verify(clientsMapper, times(1)).toAdminResponseList(mockClients);
    }

    @Test
    @DisplayName("getClientsByStatus - проверка преобразования статуса в верхний регистр")
    void getClientsByStatus_ShouldConvertToUpperCase() {
        String statusInput = "new";
        Status expectedStatus = Status.NEW;

        when(clientsRepository.findByStatus(eq(expectedStatus), any(Sort.class)))
                .thenReturn(List.of());
        when(clientsMapper.toAdminResponseList(anyList()))
                .thenReturn(List.of());

        clientsService.getClientsByStatus(statusInput);

        verify(clientsRepository).findByStatus(eq(expectedStatus), any(Sort.class));
    }

    @Test
    @DisplayName("getClientsByStatus - проверка сортировки по createdAt DESC")
    void getClientsByStatus_ShouldSortByCreatedAtDesc() {
        String statusInput = "new";
        Status expectedStatus = Status.NEW;

        when(clientsRepository.findByStatus(eq(expectedStatus), any(Sort.class)))
                .thenReturn(List.of());
        when(clientsMapper.toAdminResponseList(anyList()))
                .thenReturn(List.of());

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);

        clientsService.getClientsByStatus(statusInput);

        verify(clientsRepository).findByStatus(eq(expectedStatus), sortCaptor.capture());
        Sort sort = sortCaptor.getValue();
        assertNotNull(sort.getOrderFor("createdAt"));
        assertEquals(Sort.Direction.DESC, sort.getOrderFor("createdAt").getDirection());
    }

    @Test
    @DisplayName("getClientsByStatus - с некорректным статусом выбрасывает ValidationException")
    void getClientsByStatus_WithInvalidStatus_ShouldThrowValidationException() {
        String invalidStatus = "INVALID_STATUS";

        ValidationException exception = assertThrows(ValidationException.class,
                () -> clientsService.getClientsByStatus(invalidStatus));

        assertTrue(exception.getMessage().contains("Некорректный статус: " + invalidStatus));
        verify(clientsRepository, never()).findByStatus(any(), any());
        verify(clientsMapper, never()).toAdminResponseList(any());
    }

    @Test
    @DisplayName("getClientsByStatus - с пустым статусом выбрасывает ValidationException")
    void getClientsByStatus_WithEmptyStatus_ShouldThrowValidationException() {
        String emptyStatus = "   ";

        ValidationException exception = assertThrows(ValidationException.class,
                () -> clientsService.getClientsByStatus(emptyStatus));

        assertTrue(exception.getMessage().contains("Статус не может быть пустым"));
        verify(clientsRepository, never()).findByStatus(any(), any());
    }

    @Test
    @DisplayName("getClientsByStatus - пустой результат возвращает пустой список")
    void getClientsByStatus_EmptyResult_ShouldReturnEmptyList() {
        String statusInput = "done";
        Status expectedStatus = Status.DONE;

        when(clientsRepository.findByStatus(eq(expectedStatus), any(Sort.class)))
                .thenReturn(List.of());
        when(clientsMapper.toAdminResponseList(List.of()))
                .thenReturn(List.of());

        List<ClientsAdminRsDto> result = clientsService.getClientsByStatus(statusInput);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clientsRepository).findByStatus(eq(expectedStatus), any(Sort.class));
        verify(clientsMapper).toAdminResponseList(List.of());
    }

    @Test
    @DisplayName("getClientsByStatus - исключение БД пробрасывается как RuntimeException")
    void getClientsByStatus_RepositoryThrowsException_ShouldThrowRuntimeException() {
        String statusInput = "new";
        Status expectedStatus = Status.NEW;

        when(clientsRepository.findByStatus(eq(expectedStatus), any(Sort.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clientsService.getClientsByStatus(statusInput));

        assertTrue(exception.getMessage().contains("Внутренняя ошибка сервера"));
        verify(clientsMapper, never()).toAdminResponseList(any());
    }

    @Test
    @DisplayName("updateClient - успешное обновление всех полей")
    void updateClient_WithFullUpdate_ShouldUpdateAllFields() {
        when(clientsRepository.findById(testClientId)).thenReturn(Optional.of(existingClient));
        doNothing().when(clientsMapper).updateEntity(existingClient, fullUpdateDto);
        when(clientsRepository.save(existingClient)).thenReturn(updatedClient);
        when(clientsMapper.toAdminResponse(updatedClient)).thenReturn(responseDto);

        when(clientsRepository.count()).thenReturn(100L);
        when(clientsRepository.countByStatus(Status.NEW)).thenReturn(10L);
        when(clientsRepository.countByStatus(Status.PROCESSED)).thenReturn(20L);
        when(clientsRepository.countByStatus(Status.DONE)).thenReturn(70L);
        when(clientsRepository.countByCreatedAtBetween(any(), any())).thenReturn(5L);

        ClientsAdminRsDto result = clientsService.updateClient(testClientId, fullUpdateDto);

        assertNotNull(result);
        assertEquals(testClientId, result.getId());
        assertEquals("Иван Иванов", result.getName());
        assertEquals(Status.PROCESSED, result.getStatus());

        verify(clientsMapper, times(1)).updateEntity(existingClient, fullUpdateDto);

        verify(clientsRepository, times(1)).save(existingClient);

        verify(webSocketPublisher, times(1)).publishApplicationUpdated(responseDto);
        verify(webSocketPublisher, times(1)).publishStatsUpdated(any(ClientsStatsRsDto.class));
    }

    @Test
    @DisplayName("updateClient - частичное обновление только статуса")
    void updateClient_WithPartialUpdate_ShouldUpdateOnlyStatus() {
        Clients partiallyUpdatedClient = new Clients();
        partiallyUpdatedClient.setId(testClientId);
        partiallyUpdatedClient.setName("Иван Петров"); // осталось старым
        partiallyUpdatedClient.setEmail("ivan@example.com"); // осталось старым
        partiallyUpdatedClient.setStatus(Status.DONE);
        partiallyUpdatedClient.setPriority(Priority.MEDIUM); // остался старым
        partiallyUpdatedClient.setProcessedBy("admin@covenantcode.ru");
        partiallyUpdatedClient.setProcessedAt(OffsetDateTime.now());

        ClientsAdminRsDto partialResponseDto = new ClientsAdminRsDto();
        partialResponseDto.setId(testClientId);
        partialResponseDto.setName("Иван Петров");
        partialResponseDto.setStatus(Status.DONE);
        partialResponseDto.setStatusLabel("Обработано");

        when(clientsRepository.findById(testClientId)).thenReturn(Optional.of(existingClient));
        doNothing().when(clientsMapper).updateEntity(existingClient, partialUpdateDto);
        when(clientsRepository.save(existingClient)).thenReturn(partiallyUpdatedClient);
        when(clientsMapper.toAdminResponse(partiallyUpdatedClient)).thenReturn(partialResponseDto);

        when(clientsRepository.count()).thenReturn(100L);
        when(clientsRepository.countByStatus(any())).thenReturn(0L);
        when(clientsRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);

        ClientsAdminRsDto result = clientsService.updateClient(testClientId, partialUpdateDto);

        assertNotNull(result);
        assertEquals(testClientId, result.getId());
        assertEquals("Иван Петров", result.getName()); // имя не изменилось
        assertEquals(Status.DONE, result.getStatus());

        assertNotNull(partiallyUpdatedClient.getProcessedBy());
        assertNotNull(partiallyUpdatedClient.getProcessedAt());

        verify(clientsMapper, times(1)).updateEntity(existingClient, partialUpdateDto);
        verify(clientsRepository, times(1)).save(existingClient);
        verify(webSocketPublisher, times(1)).publishApplicationUpdated(partialResponseDto);
    }

    @Test
    @DisplayName("updateClient - клиент не найден, выбрасывает ClientNotFoundException")
    void updateClient_WhenClientNotFound_ShouldThrowClientNotFoundException() {
        // Given
        when(clientsRepository.findById(testClientId)).thenReturn(Optional.empty());

        // When/Then
        ClientNotFoundException exception = assertThrows(ClientNotFoundException.class,
                () -> clientsService.updateClient(testClientId, fullUpdateDto));

        assertTrue(exception.getMessage().contains(testClientId.toString()));
        verify(clientsRepository, never()).save(any());
        verify(clientsMapper, never()).updateEntity(any(), any());
        verifyNoInteractions(webSocketPublisher);
    }

    @Test
    @DisplayName("updateClient - email без @ вызывает ValidationException")
    void updateClient_WithInvalidEmail_ShouldThrowValidationException() {
        ClientsUpdateRqDto invalidDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("invalid-email") // нет @
                .build();

        when(clientsRepository.findById(testClientId)).thenReturn(Optional.of(existingClient));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> clientsService.updateClient(testClientId, invalidDto));

        assertTrue(exception.getMessage().contains("Email должен содержать символ '@'"));
        verify(clientsRepository, never()).save(any());
        verify(clientsMapper, never()).updateEntity(any(), any());
    }

    @Test
    @DisplayName("updateClient - невалидный телефон вызывает ValidationException")
    void updateClient_WithInvalidPhone_ShouldThrowValidationException() {
        ClientsUpdateRqDto invalidDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .phone("12345") // неверный формат
                .build();

        when(clientsRepository.findById(testClientId)).thenReturn(Optional.of(existingClient));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> clientsService.updateClient(testClientId, invalidDto));

        assertTrue(exception.getMessage().contains("Телефон должен быть в формате +7XXXXXXXXXX"));
        verify(clientsRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateClient - невалидный статус вызывает ValidationException")
    void updateClient_WithInvalidStatus_ShouldThrowValidationException() {
        ClientsUpdateRqDto invalidDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .status("INVALID_STATUS")
                .build();

        when(clientsRepository.findById(testClientId)).thenReturn(Optional.of(existingClient));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> clientsService.updateClient(testClientId, invalidDto));

        assertTrue(exception.getMessage().contains("Некорректный статус: INVALID_STATUS"));
        verify(clientsRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateClient - невалидный приоритет вызывает ValidationException")
    void updateClient_WithInvalidPriority_ShouldThrowValidationException() {
        ClientsUpdateRqDto invalidDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .priority("INVALID_PRIORITY")
                .build();

        when(clientsRepository.findById(testClientId)).thenReturn(Optional.of(existingClient));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> clientsService.updateClient(testClientId, invalidDto));

        assertTrue(exception.getMessage().contains("Некорректный приоритет: INVALID_PRIORITY"));
        verify(clientsRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateClient - невалидный тип курса вызывает ValidationException")
    void updateClient_WithInvalidCourseType_ShouldThrowValidationException() {
        ClientsUpdateRqDto invalidDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .courseType("INVALID_COURSE")
                .build();

        when(clientsRepository.findById(testClientId)).thenReturn(Optional.of(existingClient));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> clientsService.updateClient(testClientId, invalidDto));

        assertTrue(exception.getMessage().contains("Некорректный тип курса: INVALID_COURSE"));
        verify(clientsRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateClient - ошибка БД при сохранении выбрасывает PersistenceException")
    void updateClient_WhenDatabaseError_ShouldThrowPersistenceException() {
        when(clientsRepository.findById(testClientId)).thenReturn(Optional.of(existingClient));
        doNothing().when(clientsMapper).updateEntity(existingClient, fullUpdateDto);
        when(clientsRepository.save(existingClient))
                .thenThrow(new RuntimeException("Database error"));

        PersistenceException exception = assertThrows(PersistenceException.class,
                () -> clientsService.updateClient(testClientId, fullUpdateDto));

        assertTrue(exception.getMessage().contains("Clients"));
        assertTrue(exception.getMessage().contains("обновление"));

        verify(clientsMapper, times(1)).updateEntity(existingClient, fullUpdateDto);
        verify(clientsRepository, times(1)).save(existingClient);
        verify(webSocketPublisher, never()).publishApplicationUpdated(any());
    }

    @Test
    @DisplayName("updateClient - проверка установки processedAt при указании processedBy")
    void updateClient_WhenProcessedByProvided_ShouldSetProcessedAt() {
        // Given
        ClientsUpdateRqDto updateWithProcessedBy = ClientsUpdateRqDto.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .processedBy("admin@covenantcode.ru")
                .build();

        when(clientsRepository.findById(testClientId)).thenReturn(Optional.of(existingClient));

        Clients updatedWithProcessed = new Clients();
        updatedWithProcessed.setId(testClientId);
        updatedWithProcessed.setName("Иван Иванов");
        updatedWithProcessed.setProcessedBy("admin@covenantcode.ru");
        updatedWithProcessed.setProcessedAt(OffsetDateTime.now());

        doAnswer(invocation -> {
            Clients client = invocation.getArgument(0);
            ClientsUpdateRqDto dto = invocation.getArgument(1);

            // Имитируем логику updateEntity
            if (dto.getName() != null) {
                client.setName(dto.getName());
            }
            if (dto.getEmail() != null) {
                client.setEmail(dto.getEmail());
            }
            // Имитируем @AfterMapping логику
            if (dto.getProcessedBy() != null && !dto.getProcessedBy().isBlank()) {
                client.setProcessedBy(dto.getProcessedBy());
                client.setProcessedAt(OffsetDateTime.now());
            }
            return null;
        }).when(clientsMapper).updateEntity(any(Clients.class), any(ClientsUpdateRqDto.class));

        when(clientsRepository.save(any(Clients.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientsMapper.toAdminResponse(any(Clients.class))).thenReturn(responseDto);

        // Мок для getStats()
        when(clientsRepository.count()).thenReturn(100L);
        when(clientsRepository.countByStatus(any())).thenReturn(0L);
        when(clientsRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);

        // When
        clientsService.updateClient(testClientId, updateWithProcessedBy);

        // Then
        ArgumentCaptor<Clients> clientCaptor = ArgumentCaptor.forClass(Clients.class);
        verify(clientsRepository).save(clientCaptor.capture());
        Clients savedClient = clientCaptor.getValue();

        assertNotNull(savedClient.getProcessedBy(), "processedBy should not be null");
        assertEquals("admin@covenantcode.ru", savedClient.getProcessedBy());
        assertNotNull(savedClient.getProcessedAt(), "processedAt should not be null");
    }

    @Test
    @DisplayName("updateClient - проверка вызова validateClientUpdate")
    void updateClient_ShouldCallValidateClientUpdate() {
        ClientsUpdateRqDto validDto = ClientsUpdateRqDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .build();

        when(clientsRepository.findById(testClientId)).thenReturn(Optional.of(existingClient));
        doNothing().when(clientsMapper).updateEntity(existingClient, validDto);
        when(clientsRepository.save(existingClient)).thenReturn(existingClient);
        when(clientsMapper.toAdminResponse(existingClient)).thenReturn(responseDto);

        when(clientsRepository.count()).thenReturn(100L);
        when(clientsRepository.countByStatus(any())).thenReturn(0L);
        when(clientsRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);

        clientsService.updateClient(testClientId, validDto);

        verify(clientsMapper, times(1)).updateEntity(existingClient, validDto);
        verify(clientsRepository, times(1)).save(existingClient);
    }
}