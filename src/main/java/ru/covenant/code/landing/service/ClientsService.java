package ru.covenant.code.landing.service;

import ru.covenant.code.landing.dto.request.ClientsRqDto;
import ru.covenant.code.landing.dto.request.ClientsStatusRqDto;
import ru.covenant.code.landing.dto.response.ClientsAdminRsDto;
import ru.covenant.code.landing.dto.response.ClientsCreateRsDto;
import ru.covenant.code.landing.entity.Clients;

import java.util.List;
import java.util.UUID;


public interface ClientsService {

    Clients getById(UUID id);

    ClientsCreateRsDto create(ClientsRqDto request);

    List<Clients> getAll();

    Clients updateStatus(UUID id, ClientsStatusRqDto clientsStatusRqDto);

    void delete(UUID id);

    List<ClientsAdminRsDto> getAllAdminClients();
}
