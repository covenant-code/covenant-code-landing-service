package ru.covenant.code.landing.service.client;

import ru.covenant.code.landing.dto.client.request.ClientsRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsCreateRsDto;

public interface ClientsService {

    ClientsCreateRsDto create(ClientsRqDto dto);
}
