package ru.covenant.code.landing.mapper;

import org.junit.jupiter.api.Test;
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
}