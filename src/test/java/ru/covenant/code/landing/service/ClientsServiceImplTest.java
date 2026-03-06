package ru.covenant.code.landing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.covenant.code.landing.dto.client.request.ClientsRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.dto.client.response.ClientsCreateRsDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.exceptions.ClientDuplicateException;
import ru.covenant.code.landing.exceptions.PersistenceException;
import ru.covenant.code.landing.mapper.clients.ClientsMapper;
import ru.covenant.code.landing.repository.ClientsRepository;
import ru.covenant.code.landing.service.client.impl.ClientsServiceImpl;
import ru.covenant.code.landing.ws.service.WebSocketPublisher;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientsServiceImplTest {

    @Mock
    private ClientsRepository clientsRepository;

    @Mock
    private ClientsMapper clientsMapper;

    @Mock
    private WebSocketPublisher webSocketPublisher;

    @InjectMocks
    private ClientsServiceImpl clientsService;

    private ClientsRqDto buildDto(String email, String phone) {
        return ClientsRqDto.builder()
                .name("Иван Петров")
                .email(email)
                .phone(phone)
                .message("Тестовое сообщение")
                .courseType("BACKEND")
                .source("Лендинг")
                .build();
    }

    private Clients buildEntity(UUID id) {
        Clients entity = new Clients();
        entity.setId(id);
        entity.setName("Иван Петров");
        entity.setEmail("ivan@example.com");
        entity.setPhone("+79161234567");
        entity.setCourseType(CourseType.BACKEND);
        entity.setStatus(Status.NEW);
        entity.setPriority(Priority.MEDIUM);
        entity.setSource("Лендинг");
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        return entity;
    }

    @Test
    void create_validDto_shouldSaveAndReturnDto() {
        // given
        UUID id = UUID.randomUUID();
        ClientsRqDto dto = buildDto("ivan@example.com", "+79161234567");
        Clients entity = buildEntity(id);

        ClientsCreateRsDto expectedResponse = new ClientsCreateRsDto();
        expectedResponse.setId(id);
        expectedResponse.setName("Иван Петров");
        expectedResponse.setEmail("ivan@example.com");
        expectedResponse.setCourseType(CourseType.BACKEND);
        expectedResponse.setStatus(Status.NEW);

        when(clientsRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(clientsMapper.toNewEntity(dto)).thenReturn(entity);
        when(clientsRepository.save(entity)).thenReturn(entity);
        when(clientsMapper.toCreateResponse(entity)).thenReturn(expectedResponse);
        when(clientsMapper.toAdminResponse(entity)).thenReturn(new ClientsAdminRsDto());

        when(clientsRepository.count()).thenReturn(1L);
        when(clientsRepository.countByStatus(any())).thenReturn(0L);
        when(clientsRepository.countByCourseType(any())).thenReturn(0L);
        when(clientsRepository.countByPriority(any())).thenReturn(0L);
        when(clientsRepository.countByCreatedAtBetween(any(), any())).thenReturn(1L);

        // when
        ClientsCreateRsDto result = clientsService.create(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getEmail()).isEqualTo("ivan@example.com");

        verify(clientsRepository).save(entity);
        verify(webSocketPublisher).publishApplicationCreated(any());
        verify(webSocketPublisher).publishStatsUpdated(any());
    }

    @Test
    void create_duplicateEmail_shouldThrowClientDuplicateException() {
        // given
        ClientsRqDto dto = buildDto("duplicate@example.com", "+79161234567");
        when(clientsRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> clientsService.create(dto))
                .isInstanceOf(ClientDuplicateException.class)
                .satisfies(ex -> {
                    ClientDuplicateException dupe = (ClientDuplicateException) ex;
                    assertThat(dupe.getEmail()).isEqualTo("duplicate@example.com");
                });

        verify(clientsRepository, never()).save(any());
        verify(webSocketPublisher, never()).publishApplicationCreated(any());
    }

    @Test
    void create_repositorySaveThrows_shouldThrowPersistenceException() {
        // given
        ClientsRqDto dto = buildDto("ivan@example.com", "+79161234567");
        Clients entity = buildEntity(UUID.randomUUID());

        when(clientsRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(clientsMapper.toNewEntity(dto)).thenReturn(entity);
        when(clientsRepository.save(entity)).thenThrow(new RuntimeException("DB connection failed"));

        // when / then
        assertThatThrownBy(() -> clientsService.create(dto))
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    void create_withoutOptionalFields_shouldSaveCorrectly() {
        // given
        UUID id = UUID.randomUUID();
        ClientsRqDto dto = ClientsRqDto.builder()
                .name("Анна Смирнова")
                .email("anna@example.com")
                .courseType("FRONTEND")
                .build();

        Clients entity = buildEntity(id);
        entity.setPhone(null);
        entity.setMessage(null);

        ClientsCreateRsDto expectedResponse = new ClientsCreateRsDto();
        expectedResponse.setId(id);

        when(clientsRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(clientsMapper.toNewEntity(dto)).thenReturn(entity);
        when(clientsRepository.save(entity)).thenReturn(entity);
        when(clientsMapper.toCreateResponse(entity)).thenReturn(expectedResponse);
        when(clientsMapper.toAdminResponse(entity)).thenReturn(new ClientsAdminRsDto());

        when(clientsRepository.count()).thenReturn(1L);
        when(clientsRepository.countByStatus(any())).thenReturn(0L);
        when(clientsRepository.countByCourseType(any())).thenReturn(0L);
        when(clientsRepository.countByPriority(any())).thenReturn(0L);
        when(clientsRepository.countByCreatedAtBetween(any(), any())).thenReturn(1L);

        // when
        ClientsCreateRsDto result = clientsService.create(dto);

        // then
        assertThat(result).isNotNull();
        verify(clientsRepository).save(entity);
    }
}
