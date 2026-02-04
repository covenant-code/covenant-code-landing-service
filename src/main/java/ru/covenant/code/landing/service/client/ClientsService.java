package ru.covenant.code.landing.service.client;

import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.dto.client.request.ClientsRqDto;
import ru.covenant.code.landing.dto.client.request.ClientsStatusRqDto;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.dto.client.response.ClientsCreateRsDto;
import ru.covenant.code.landing.dto.client.response.ClientsStatsRsDto;

import java.util.List;
import java.util.UUID;

public interface ClientsService {
    ClientsCreateRsDto create(ClientsRqDto dto);

    List<ClientsAdminRsDto> getAllClients(ClientsFilterRqDto filter);

    ClientsAdminRsDto getClientById(UUID id);

    ClientsAdminRsDto updateStatus(UUID id, ClientsStatusRqDto dto);

    void delete(UUID id);

    List<ClientsAdminRsDto> getClientsByStatus(String status);

    ClientsStatsRsDto getStats();

    ClientsAdminRsDto updateClient(UUID id, ClientsUpdateRqDto dto);
}