package ru.covenant.code.landing.service.client.impl;

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
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.exceptions.BusinessException;
import ru.covenant.code.landing.exceptions.ValidationException;
import ru.covenant.code.landing.mapper.ClientsMapper;
import ru.covenant.code.landing.repository.ClientsRepository;
import ru.covenant.code.landing.specification.ClientsSpecification;

import java.util.List;

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

    @InjectMocks
    private ClientsServiceImpl clientsService;

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
}