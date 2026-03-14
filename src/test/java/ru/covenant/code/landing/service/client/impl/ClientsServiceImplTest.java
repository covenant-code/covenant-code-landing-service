package ru.covenant.code.landing.service.client.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.exceptions.BusinessException;
import ru.covenant.code.landing.mapper.ClientsMapper;
import ru.covenant.code.landing.repository.ClientsRepository;
import ru.covenant.code.landing.specification.ClientsSpecification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
}