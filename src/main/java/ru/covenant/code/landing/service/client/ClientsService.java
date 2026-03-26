package ru.covenant.code.landing.service.client;

import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.dto.client.request.ClientsUpdateRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.entity.Clients;

import java.util.List;
import java.util.UUID;

public interface ClientsService {
    List<ClientsAdminRsDto> getAllClients(ClientsFilterRqDto filter);
    List<ClientsAdminRsDto> getClientsByStatus(String status);
    ClientsAdminRsDto updateClient(UUID id, ClientsUpdateRqDto dto);

    ClientsAdminRsDto getClientById(UUID id);

}
