package ru.covenant.code.landing.service.client;

import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;

import java.util.List;

public interface ClientsService {
    List<ClientsAdminRsDto> getAllClients(ClientsFilterRqDto filter);
}
