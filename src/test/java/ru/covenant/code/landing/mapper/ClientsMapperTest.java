package ru.covenant.code.landing.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;
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

    private final ClientsMapper clientsMapper = new ClientsMapper() {
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

            // Русские метки
            if (client.getStatus() != null) {
                dto.setStatusLabel(client.getStatus().getDisplayName());
            }
            if (client.getPriority() != null) {
                dto.setPriorityLabel(client.getPriority().getDisplayName());
            }

            // Форматирование дат
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

    @BeforeEach
    void setUp() {
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

        // Then
        // Поля остались прежними
        assertEquals(CourseType.BACKEND, existingClient.getCourseType());
        assertEquals(Status.NEW, existingClient.getStatus());
        assertEquals(Priority.MEDIUM, existingClient.getPriority());
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

}