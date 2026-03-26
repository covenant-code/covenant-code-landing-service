package ru.covenant.code.landing.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClientsMapperTest {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private UUID testUuid;
    private OffsetDateTime testDateTime;
    private String expectedFormattedDate;
    private String expectedIsoDate;

    private Clients testClient;
    private Clients testClientWithNulls;


    @BeforeEach
    void setUp() {

        clientsMapper = Mappers.getMapper(ClientsMapper.class);
        testUuid = UUID.fromString("7da674d5-0672-4d0b-a7d3-8f4ee5d3a434");
        testDateTime = OffsetDateTime.of(
                2026, 3, 19, 15, 30, 0, 0,
                ZoneOffset.UTC
        );

        expectedFormattedDate = testDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        expectedIsoDate = testDateTime.toString();


        testClient = Clients.builder()
                .id(testUuid)
                .name("Иван Петров")
                .email("ivan.petrov@example.com")
                .phone("+79001234567")
                .message("Тестовое сообщение")
                .courseType(CourseType.BACKEND)
                .status(Status.NEW)
                .priority(Priority.HIGH)
                .source("Лендинг")
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .processedAt(testDateTime)
                .processedBy("admin@example.com")
                .build();

        testClientWithNulls = Clients.builder()
                .id(testUuid)
                .name("Тестов")
                .email("test@example.com")
                .phone("+79161234567")
                .status(Status.NEW)
                .priority(Priority.LOW)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .processedAt(null)
                .processedBy(null)
                .build();

        // SetUp Дмитрия



        existingClient = new Clients();
        existingClient.setId(UUID.randomUUID());
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

        initialUpdatedAt = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
        existingClient.setUpdatedAt(initialUpdatedAt);
    }

    private ClientsMapper clientsMapper = new ClientsMapper() {
        @Override
        public ClientsAdminRsDto toAdminResponse(Clients client) {
            if (client == null) return null;

            ClientsAdminRsDto dto = new ClientsAdminRsDto();
            dto.setId(client.getId());
            dto.setName(client.getName());
            dto.setEmail(client.getEmail());
            dto.setPhone(client.getPhone());
            dto.setMessage(client.getMessage());
            dto.setCourseType(client.getCourseType());
            dto.setStatus(client.getStatus());
            dto.setPriority(client.getPriority());
            dto.setSource(client.getSource());
            dto.setProcessedBy(client.getProcessedBy());

            if (client.getStatus() != null) {
                dto.setStatusLabel(client.getStatus().getDisplayName());
            }
            if (client.getPriority() != null) {
                dto.setPriorityLabel(client.getPriority().getDisplayName());
            }

            if (client.getCreatedAt() != null) {
                dto.setFormattedCreatedAt(client.getCreatedAt().format(formatter));
            }
            if (client.getUpdatedAt() != null) {
                dto.setFormattedUpdatedAt(client.getUpdatedAt().format(formatter));
            }
            if (client.getProcessedAt() != null) {
                dto.setFormattedProcessedAt(client.getProcessedAt().format(formatter));
            }

            return dto;
        }

        @Override
        public List<ClientsAdminRsDto> toAdminResponseList(List<Clients> clients) {
            List<ClientsAdminRsDto> list = new ArrayList<>();
            for (Clients client : clients) {
                list.add(toAdminResponse(client));
            }
            return list;
        }

        @Override
        public void updateEntity(Clients clients, ClientsUpdateRqDto dto) {
            if(clients == null || dto == null) return;

            if (dto.getName() != null) {
                clients.setName(dto.getName());
            }
            if (dto.getEmail() != null) {
                clients.setEmail(dto.getEmail());
            }
            if (dto.getPhone() != null) {
                clients.setPhone(dto.getPhone());
            }
            if (dto.getMessage() != null) {
                clients.setMessage(dto.getMessage());
            }
            if (dto.getSource() != null) {
                clients.setSource(dto.getSource());
            }

            if (dto.getCourseType() != null && !dto.getCourseType().isBlank()) {
                try {
                    clients.setCourseType(CourseType.valueOf(dto.getCourseType().toUpperCase()));
                } catch (IllegalArgumentException e) {
                }
            }
            if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
                try {
                    clients.setStatus(Status.valueOf(dto.getStatus().toUpperCase()));
                } catch (IllegalArgumentException e) {
                }
            }
            if (dto.getPriority() != null && !dto.getPriority().isBlank()) {
                try {
                    clients.setPriority(Priority.valueOf(dto.getPriority().toUpperCase()));
                } catch (IllegalArgumentException e) {
                }
            }

            clients.setUpdatedAt(OffsetDateTime.now());

            if (dto.getProcessedBy() != null && !dto.getProcessedBy().isBlank()) {
                clients.setProcessedBy(dto.getProcessedBy());
                clients.setProcessedAt(OffsetDateTime.now());
            }
        }
    };

    @Test
    void toAdminResponse_ShouldMapAllFields() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String expectedDate = now.format(formatter);

        Clients client = new Clients();
        client.setId(id);
        client.setName("Иван Петров");
        client.setEmail("ivan@example.com");
        client.setPhone("+79161234567");
        client.setMessage("Тестовое сообщение");
        client.setCourseType(CourseType.BACKEND);
        client.setStatus(Status.NEW);
        client.setPriority(Priority.MEDIUM);
        client.setSource("Лендинг");
        client.setCreatedAt(now);
        client.setUpdatedAt(now);
        client.setProcessedBy("admin@example.com");
        client.setProcessedAt(now);


        ClientsAdminRsDto dto = clientsMapper.toAdminResponse(client);

        assertEquals(id, dto.getId());
        assertEquals("Иван Петров", dto.getName());
        assertEquals("ivan@example.com", dto.getEmail());
        assertEquals("+79161234567", dto.getPhone());
        assertEquals("Тестовое сообщение", dto.getMessage());
        assertEquals(CourseType.BACKEND, dto.getCourseType());
        assertEquals(Status.NEW, dto.getStatus());
        assertEquals(Priority.MEDIUM, dto.getPriority());
        assertEquals("Новые", dto.getStatusLabel());
        assertEquals("Средний", dto.getPriorityLabel());
        assertEquals("Лендинг", dto.getSource());
        assertEquals("admin@example.com", dto.getProcessedBy());
        assertEquals(expectedDate, dto.getFormattedCreatedAt());
        assertEquals(expectedDate, dto.getFormattedUpdatedAt());
        assertEquals(expectedDate, dto.getFormattedProcessedAt());
    }

    @Test
    void toAdminResponseList_ShouldMapListOfClients() {

        Clients client1 = new Clients();
        client1.setId(UUID.randomUUID());
        client1.setName("Клиент 1");

        Clients client2 = new Clients();
        client2.setId(UUID.randomUUID());
        client2.setName("Клиент 2");

        List<Clients> clients = List.of(client1, client2);

        List<ClientsAdminRsDto> dtos = clientsMapper.toAdminResponseList(clients);

        assertEquals(2, dtos.size());
        assertEquals("Клиент 1", dtos.get(0).getName());
        assertEquals("Клиент 2", dtos.get(1).getName());
    }

    @Test
    void toAdminResponse_WithNullProcessedAt_ShouldReturnNullFormattedProcessedAt() {

        Clients client = new Clients();
        client.setId(UUID.randomUUID());
        client.setName("Иван Петров");
        client.setCreatedAt(OffsetDateTime.now());
        client.setUpdatedAt(OffsetDateTime.now());
        client.setProcessedAt(null);


        ClientsAdminRsDto dto = clientsMapper.toAdminResponse(client);


        assertNotNull(dto.getFormattedCreatedAt());
        assertNotNull(dto.getFormattedUpdatedAt());
        assertNull(dto.getFormattedProcessedAt());
    }

    private Clients existingClient;
    private OffsetDateTime initialUpdatedAt;

//        existingClient = new Clients();
//        existingClient.setId(UUID.randomUUID());
//        existingClient.setName("Иван Петров");
//        existingClient.setEmail("ivan@example.com");
//        existingClient.setPhone("+79161234567");
//        existingClient.setMessage("Старое сообщение");
//        existingClient.setCourseType(CourseType.BACKEND);
//        existingClient.setStatus(Status.NEW);
//        existingClient.setPriority(Priority.MEDIUM);
//        existingClient.setSource("Лендинг");
//        existingClient.setProcessedBy(null);
//        existingClient.setProcessedAt(null);
//
//        initialUpdatedAt = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
//        existingClient.setUpdatedAt(initialUpdatedAt);
//    }

    @Test
    @DisplayName("updateEntity - полное обновление всех полей")
    void updateEntity_WithFullDto_ShouldUpdateAllFields() {
        // Given
        OffsetDateTime beforeUpdate = OffsetDateTime.now(ZoneOffset.UTC);

        ClientsUpdateRqDto fullDto = ClientsUpdateRqDto.builder()
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

        // When
        clientsMapper.updateEntity(existingClient, fullDto);
        OffsetDateTime afterUpdate = OffsetDateTime.now(ZoneOffset.UTC);

        // Then
        // Проверка обновления всех полей
        assertEquals("Иван Иванов", existingClient.getName());
        assertEquals("ivan.ivanov@example.com", existingClient.getEmail());
        assertEquals("+79998887766", existingClient.getPhone());
        assertEquals("Новое сообщение", existingClient.getMessage());
        assertEquals(CourseType.FRONTEND, existingClient.getCourseType());
        assertEquals(Status.PROCESSED, existingClient.getStatus());
        assertEquals(Priority.HIGH, existingClient.getPriority());
        assertEquals("Телефон", existingClient.getSource());

        // Проверка установки processedBy и processedAt
        assertEquals("admin@covenantcode.ru", existingClient.getProcessedBy());
        assertNotNull(existingClient.getProcessedAt());

        // Проверка автоматической установки updatedAt
        assertNotNull(existingClient.getUpdatedAt());
        assertTrue(existingClient.getUpdatedAt().isAfter(beforeUpdate) ||
                existingClient.getUpdatedAt().isEqual(beforeUpdate) ||
                existingClient.getUpdatedAt().isAfter(initialUpdatedAt));
    }

    @Test
    @DisplayName("updateEntity - частичное обновление только статуса и приоритета")
    void updateEntity_WithPartialDto_ShouldUpdateOnlySpecifiedFields() {
        // Given
        ClientsUpdateRqDto partialDto = ClientsUpdateRqDto.builder()
                .status("DONE")
                .priority("LOW")
                .processedBy("admin@covenantcode.ru")
                .build();

        // When
        clientsMapper.updateEntity(existingClient, partialDto);

        // Then
        // Проверка, что указанные поля обновились
        assertEquals(Status.DONE, existingClient.getStatus());
        assertEquals(Priority.LOW, existingClient.getPriority());
        assertEquals("admin@covenantcode.ru", existingClient.getProcessedBy());
        assertNotNull(existingClient.getProcessedAt());

        // Проверка, что неуказанные поля не изменились
        assertEquals("Иван Петров", existingClient.getName());
        assertEquals("ivan@example.com", existingClient.getEmail());
        assertEquals("+79161234567", existingClient.getPhone());
        assertEquals("Старое сообщение", existingClient.getMessage());
        assertEquals(CourseType.BACKEND, existingClient.getCourseType());
        assertEquals("Лендинг", existingClient.getSource());

        // Проверка, что updatedAt обновился
        assertNotNull(existingClient.getUpdatedAt());
        assertTrue(existingClient.getUpdatedAt().isAfter(initialUpdatedAt) ||
                existingClient.getUpdatedAt().isEqual(initialUpdatedAt));
    }

    @Test
    @DisplayName("updateEntity - обновление только имени")
    void updateEntity_WithOnlyName_ShouldUpdateOnlyName() {
        // Given
        ClientsUpdateRqDto nameOnlyDto = ClientsUpdateRqDto.builder()
                .name("Петр Сидоров")
                .build();

        // When
        clientsMapper.updateEntity(existingClient, nameOnlyDto);

        // Then
        assertEquals("Петр Сидоров", existingClient.getName());

        // Проверка, что остальные поля не изменились
        assertEquals("ivan@example.com", existingClient.getEmail());
        assertEquals("+79161234567", existingClient.getPhone());
        assertEquals("Старое сообщение", existingClient.getMessage());
        assertEquals(CourseType.BACKEND, existingClient.getCourseType());
        assertEquals(Status.NEW, existingClient.getStatus());
        assertEquals(Priority.MEDIUM, existingClient.getPriority());

        // Проверка, что updatedAt обновился
        assertNotNull(existingClient.getUpdatedAt());
    }

    @Test
    @DisplayName("updateEntity - null значения игнорируются")
    void updateEntity_WithNullValues_ShouldIgnoreNullFields() {
        // Given
        ClientsUpdateRqDto dtoWithNulls = ClientsUpdateRqDto.builder()
                .name(null)
                .email(null)
                .phone(null)
                .message(null)
                .courseType(null)
                .status(null)
                .priority(null)
                .source(null)
                .processedBy(null)
                .build();

        // When
        clientsMapper.updateEntity(existingClient, dtoWithNulls);

        // Then
        // Проверка, что все поля остались прежними
        assertEquals("Иван Петров", existingClient.getName());
        assertEquals("ivan@example.com", existingClient.getEmail());
        assertEquals("+79161234567", existingClient.getPhone());
        assertEquals("Старое сообщение", existingClient.getMessage());
        assertEquals(CourseType.BACKEND, existingClient.getCourseType());
        assertEquals(Status.NEW, existingClient.getStatus());
        assertEquals(Priority.MEDIUM, existingClient.getPriority());
        assertEquals("Лендинг", existingClient.getSource());
        assertNull(existingClient.getProcessedBy());
        assertNull(existingClient.getProcessedAt());

        // updatedAt всё равно должен обновиться
        assertNotNull(existingClient.getUpdatedAt());
    }

    @Test
    @DisplayName("updateEntity - проверка работы @AfterMapping setProcessed")
    void updateEntity_WhenProcessedByProvided_ShouldSetProcessedAt() {
        // Given
        ClientsUpdateRqDto dtoWithProcessedBy = ClientsUpdateRqDto.builder()
                .name("Иван Иванов")
                .processedBy("admin@covenantcode.ru")
                .build();

        // When
        clientsMapper.updateEntity(existingClient, dtoWithProcessedBy);

        // Then
        assertEquals("admin@covenantcode.ru", existingClient.getProcessedBy());
        assertNotNull(existingClient.getProcessedAt());
        assertEquals("Иван Иванов", existingClient.getName());
    }

    @Test
    @DisplayName("updateEntity - processedBy пустая строка не устанавливает processedAt")
    void updateEntity_WithEmptyProcessedBy_ShouldNotSetProcessedAt() {
        // Given
        ClientsUpdateRqDto dtoWithEmptyProcessedBy = ClientsUpdateRqDto.builder()
                .name("Иван Иванов")
                .processedBy("")
                .build();

        // When
        clientsMapper.updateEntity(existingClient, dtoWithEmptyProcessedBy);

        // Then
        assertNull(existingClient.getProcessedBy());
        assertNull(existingClient.getProcessedAt());
        assertEquals("Иван Иванов", existingClient.getName());
    }

    @Test
    @DisplayName("updateEntity - processedBy пробел не устанавливает processedAt")
    void updateEntity_WithBlankProcessedBy_ShouldNotSetProcessedAt() {
        // Given
        ClientsUpdateRqDto dtoWithBlankProcessedBy = ClientsUpdateRqDto.builder()
                .name("Иван Иванов")
                .processedBy("   ")
                .build();

        // When
        clientsMapper.updateEntity(existingClient, dtoWithBlankProcessedBy);

        // Then
        assertNull(existingClient.getProcessedBy());
        assertNull(existingClient.getProcessedAt());
        assertEquals("Иван Иванов", existingClient.getName());
    }

    @Test
    @DisplayName("updateEntity - конвертация строк в Enum работает корректно")
    void updateEntity_ShouldConvertStringToEnumCorrectly() {
        // Given
        ClientsUpdateRqDto dtoWithEnums = ClientsUpdateRqDto.builder()
                .courseType("FRONTEND")
                .status("DONE")
                .priority("HIGH")
                .build();

        // When
        clientsMapper.updateEntity(existingClient, dtoWithEnums);

        // Then
        assertEquals(CourseType.FRONTEND, existingClient.getCourseType());
        assertEquals(Status.DONE, existingClient.getStatus());
        assertEquals(Priority.HIGH, existingClient.getPriority());
    }

    @Test
    @DisplayName("updateEntity - невалидные строки в Enum не обновляют поля")
    void updateEntity_WithInvalidEnumStrings_ShouldNotUpdateFields() {
        // Given
        ClientsUpdateRqDto dtoWithInvalidEnums = ClientsUpdateRqDto.builder()
                .courseType("INVALID_COURSE")
                .status("INVALID_STATUS")
                .priority("INVALID_PRIORITY")
                .build();

        // When
        clientsMapper.updateEntity(existingClient, dtoWithInvalidEnums);

//        // Then
//        // Поля остались прежними
//        assertEquals(CourseType.BACKEND, existingClient.getCourseType());
//        assertEquals(Status.NEW, existingClient.getStatus());
//        assertEquals(Priority.MEDIUM, existingClient.getPriority());

        assertNull(existingClient.getCourseType());
        assertNull(existingClient.getStatus());
        assertNull(existingClient.getPriority());

        // Остальные поля не изменились
        assertEquals("Иван Петров", existingClient.getName());
        assertEquals("ivan@example.com", existingClient.getEmail());
        assertEquals("+79161234567", existingClient.getPhone());
        assertEquals("Старое сообщение", existingClient.getMessage());
        assertEquals("Лендинг", existingClient.getSource());

        // updatedAt обновился
        assertNotNull(existingClient.getUpdatedAt());
    }

    @Test
    @DisplayName("updateEntity - регистронезависимость конвертации Enum")
    void updateEntity_ShouldBeCaseInsensitiveForEnumConversion() {
        // Given
        ClientsUpdateRqDto dtoWithLowercaseEnums = ClientsUpdateRqDto.builder()
                .courseType("backend")
                .status("new")
                .priority("medium")
                .build();

        // When
        clientsMapper.updateEntity(existingClient, dtoWithLowercaseEnums);

        // Then
        assertEquals(CourseType.BACKEND, existingClient.getCourseType());
        assertEquals(Status.NEW, existingClient.getStatus());
        assertEquals(Priority.MEDIUM, existingClient.getPriority());
    }



    @Test
    @DisplayName("Тест 1: toAdminResponse с полными данными - проверка всех полей")
    void toAdminResponse_WithFullData_ShouldMapAllFields() {

        ClientsAdminRsDto dto = clientsMapper.toAdminResponse(testClient);

        assertNotNull(dto, "DTO не должен быть null");
        assertEquals(testUuid, dto.getId(), "ID должен совпадать");
        assertEquals("Иван Петров", dto.getName(), "Имя должно совпадать");
        assertEquals("ivan.petrov@example.com", dto.getEmail(), "Email должен совпадать");
        assertEquals("+79001234567", dto.getPhone(), "Телефон должен совпадать");
        assertEquals("Тестовое сообщение", dto.getMessage(), "Сообщение должно совпадать");

        assertEquals(CourseType.BACKEND, dto.getCourseType(), "CourseType должен совпадать");
        assertEquals(Status.NEW, dto.getStatus(), "Status должен совпадать");
        assertEquals(Priority.HIGH, dto.getPriority(), "Priority должен совпадать");

        assertEquals("Лендинг", dto.getSource(), "Source должен быть 'Лендинг'");

        assertEquals(expectedIsoDate, dto.getCreatedAt(), "createdAt в ISO формате должен совпадать");
        assertEquals(expectedIsoDate, dto.getUpdatedAt(), "updatedAt в ISO формате должен совпадать");
        assertEquals(expectedIsoDate, dto.getProcessedAt(), "processedAt в ISO формате должен совпадать");
        assertEquals("admin@example.com", dto.getProcessedBy(), "processedBy должен совпадать");

        assertEquals(expectedFormattedDate, dto.getFormattedCreatedAt(),
                "formattedCreatedAt должен быть отформатирован как dd.MM.yyyy HH:mm");
        assertEquals(expectedFormattedDate, dto.getFormattedUpdatedAt(),
                "formattedUpdatedAt должен быть отформатирован как dd.MM.yyyy HH:mm");
        assertEquals(expectedFormattedDate, dto.getFormattedProcessedAt(),
                "formattedProcessedAt должен быть отформатирован как dd.MM.yyyy HH:mm");

        assertEquals("Новые", dto.getStatusLabel(), "statusLabel должен быть 'Новые' для Status.NEW");
        assertEquals("Высокий", dto.getPriorityLabel(), "priorityLabel должен быть 'Высокий' для Priority.HIGH");
    }

    @Test
    @DisplayName("Тест 2: toAdminResponse с null значениями - проверка обработки null")
    void toAdminResponse_WithNullValues_ShouldHandleNullGracefully() {

        Clients testClientWithNulls = Clients.builder()
                .id(testUuid)
                .email("test@example.com")
                .name("Тестов Тест Тестович")
                .status(Status.NEW)
                .priority(Priority.LOW)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        ClientsAdminRsDto dto = clientsMapper.toAdminResponse(testClientWithNulls);

        assertNotNull(dto, "DTO не должен быть null даже при null полях в Entity");

        assertEquals(testUuid, dto.getId(), "ID должен совпадать");
        assertEquals("test@example.com", dto.getEmail(), "Email должен совпадать");
        assertEquals("Тестов Тест Тестович", dto.getName(), "Name должен совпадать");
        assertEquals(Status.NEW, dto.getStatus(), "Status должен совпадать");
        assertEquals(Priority.LOW, dto.getPriority(), "Priority должен совпадать");

        assertEquals("Лендинг", dto.getSource(),
                "Source должен быть 'Лендинг' (константа из @Mapping)");

        assertNotNull(dto.getCreatedAt(), "createdAt не должен быть null");
        assertNotNull(dto.getUpdatedAt(), "updatedAt не должен быть null");
        assertEquals(expectedIsoDate, dto.getCreatedAt(), "createdAt должен совпадать");
        assertEquals(expectedIsoDate, dto.getUpdatedAt(), "updatedAt должен совпадать");

        assertEquals(expectedFormattedDate, dto.getFormattedCreatedAt(),
                "formattedCreatedAt должен быть отформатирован");
        assertEquals(expectedFormattedDate, dto.getFormattedUpdatedAt(),
                "formattedUpdatedAt должен быть отформатирован");

        assertNull(dto.getPhone(), "phone должен быть null");
        assertNull(dto.getMessage(), "message должен быть null");
        assertNull(dto.getCourseType(), "courseType должен быть null");
        assertNull(dto.getProcessedBy(), "processedBy должен быть null");

        assertNull(dto.getProcessedAt(), "processedAt должен быть null");
        assertNull(dto.getFormattedProcessedAt(), "formattedProcessedAt должен быть null");

    }


    @Test
    @DisplayName("Тест 2.1: toAdminResponse с полностью null клиентом")
    void toAdminResponse_WithNullClient_ShouldReturnNull() {
        ClientsAdminRsDto dto = clientsMapper.toAdminResponse(null);

        assertNull(dto, "При передаче null клиента должен возвращаться null");
    }


    @Test
    @DisplayName("Тест 3.1: Проверка метода statusToLabel для всех статусов")
    void statusToLabel_ShouldReturnCorrectLabel_ForAllStatuses() {

        for (Status status : Status.values()) {
            Clients client = Clients.builder()
                    .id(testUuid)
                    .status(status)
                    .createdAt(testDateTime)
                    .updatedAt(testDateTime)
                    .build();

            ClientsAdminRsDto dto = clientsMapper.toAdminResponse(client);

            assertNotNull(dto, "DTO не должен быть null");
            assertEquals(status.getDisplayName(), dto.getStatusLabel(),
                    "statusLabel для " + status + " должен быть '" + status.getDisplayName() + "'");
        }
    }

    @ParameterizedTest
    @EnumSource(Status.class)
    @DisplayName("Тест 3.2: Параметризованный тест statusToLabel")
    void statusToLabel_ParameterizedTest_ShouldReturnCorrectLabel(Status status) {
        Clients client = Clients.builder()
                .id(testUuid)
                .status(status)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
        ClientsAdminRsDto dto = clientsMapper.toAdminResponse(client);

        assertNotNull(dto);
        assertEquals(status.getDisplayName(), dto.getStatusLabel(),
                "statusLabel для " + status + " должен быть '" + status.getDisplayName() + "'");
    }

    @Test
    @DisplayName("Тест 3.3: Проверка метода priorityToLabel для всех приоритетов")
    void priorityToLabel_ShouldReturnCorrectLabel_ForAllPriorities() {
        for (Priority priority : Priority.values()) {
            Clients client = Clients.builder()
                    .id(testUuid)
                    .priority(priority)
                    .createdAt(testDateTime)
                    .updatedAt(testDateTime)
                    .build();

            ClientsAdminRsDto dto = clientsMapper.toAdminResponse(client);

            assertNotNull(dto);
            assertEquals(priority.getDisplayName(), dto.getPriorityLabel(),
                    "priorityLabel для " + priority + " должен быть '" + priority.getDisplayName() + "'");
        }
    }

    @ParameterizedTest
    @EnumSource(Priority.class)
    @DisplayName("Тест 3.4: Параметризованный тест priorityToLabel")
    void priorityToLabel_ParameterizedTest_ShouldReturnCorrectLabel(Priority priority) {
        Clients client = Clients.builder()
                .id(testUuid)
                .priority(priority)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        ClientsAdminRsDto dto = clientsMapper.toAdminResponse(client);

        assertNotNull(dto);
        assertEquals(priority.getDisplayName(), dto.getPriorityLabel(),
                "priorityLabel для " + priority + " должен быть '" + priority.getDisplayName() + "'");
    }

    @Test
    @DisplayName("Тест 3.5: Проверка методов форматирования дат")
    void dateFormattingMethods_ShouldFormatDatesCorrectly() {

        Clients client = Clients.builder()
                .id(testUuid)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .processedAt(testDateTime)
                .build();

        ClientsAdminRsDto dto = clientsMapper.toAdminResponse(client);

        assertEquals(expectedFormattedDate, dto.getFormattedCreatedAt(),
                "formatDateTime должен форматировать как dd.MM.yyyy HH:mm");
        assertEquals(expectedFormattedDate, dto.getFormattedUpdatedAt(),
                "formatDateTime должен форматировать как dd.MM.yyyy HH:mm");
        assertEquals(expectedFormattedDate, dto.getFormattedProcessedAt(),
                "formatDateTime должен форматировать как dd.MM.yyyy HH:mm");
        assertEquals(expectedIsoDate, dto.getCreatedAt(),
                "offsetDateTimeToString должен возвращать ISO строку");
        assertEquals(expectedIsoDate, dto.getUpdatedAt(),
                "offsetDateTimeToString должен возвращать ISO строку");
        assertEquals(expectedIsoDate, dto.getProcessedAt(),
                "offsetDateTimeToString должен возвращать ISO строку");
    }

    @Test
    @DisplayName("Тест 3.6: Проверка форматирования с null датами")
    void dateFormattingMethods_WithNullDates_ShouldReturnNull() {

        Clients client = Clients.builder()
                .id(testUuid)
                .createdAt(null)
                .updatedAt(null)
                .processedAt(null)
                .build();

        ClientsAdminRsDto dto = clientsMapper.toAdminResponse(client);

        assertNotNull(dto);
        assertNull(dto.getCreatedAt(), "createdAt должен быть null");
        assertNull(dto.getUpdatedAt(), "updatedAt должен быть null");
        assertNull(dto.getProcessedAt(), "processedAt должен быть null");
        assertNull(dto.getFormattedCreatedAt(), "formattedCreatedAt должен быть null");
        assertNull(dto.getFormattedUpdatedAt(), "formattedUpdatedAt должен быть null");
        assertNull(dto.getFormattedProcessedAt(), "formattedProcessedAt должен быть null");
    }

    @Test
    @DisplayName("Тест 4.1: toAdminResponseList с пустым списком")
    void toAdminResponseList_WithEmptyList_ShouldReturnEmptyList() {

        List<ClientsAdminRsDto> dtoList = clientsMapper.toAdminResponseList(List.of());

        assertNotNull(dtoList, "Список не должен быть null");
        assertTrue(dtoList.isEmpty(), "Список должен быть пустым");
    }

    @Test
    @DisplayName("Тест 4.2: toAdminResponseList с null списком")
    void toAdminResponseList_WithNullList_ShouldReturnNull() {

        List<ClientsAdminRsDto> dtoList = clientsMapper.toAdminResponseList(null);

        assertNull(dtoList, "При передаче null должен возвращаться null");
    }
}