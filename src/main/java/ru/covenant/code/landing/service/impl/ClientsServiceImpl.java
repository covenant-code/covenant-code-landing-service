package ru.covenant.code.landing.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.covenant.code.landing.dto.request.ClientsRqDto;
import ru.covenant.code.landing.dto.request.ClientsStatusRqDto;
import ru.covenant.code.landing.dto.response.ClientsAdminRsDto;
import ru.covenant.code.landing.dto.response.ClientsCreateRsDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.exceptions.ClientsNotFoundException;
import ru.covenant.code.landing.exceptions.InvalidClientsStatusException;
import ru.covenant.code.landing.exceptions.PersistenceException;
import ru.covenant.code.landing.mapper.ClientsMapper;
import ru.covenant.code.landing.repository.ClientsRepository;
import ru.covenant.code.landing.service.ClientsService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ClientsServiceImpl implements ClientsService {
    private final ClientsRepository clientsRepository;
    private final ClientsMapper clientsMapper;

    @Autowired
    public ClientsServiceImpl(ClientsRepository clientsRepository,
                              ClientsMapper clientsMapper) {
        this.clientsRepository = clientsRepository;
        this.clientsMapper = clientsMapper;
    }

    @Override
    public Clients getById(UUID id) {
        uuidIsNull(id);
        Clients client;
        try {
            client = clientsRepository.findById(id).orElseThrow(() -> new ClientsNotFoundException());
        } catch (ClientsNotFoundException e) {
            log.error("Заявка не найдена");
            throw e;
        }
        return client;
    }

    @Override
    public ClientsCreateRsDto create(ClientsRqDto request) {
        if (request == null) {
            log.error("входящий параметр = null");
            throw new IllegalArgumentException("request = null");
        }
        Clients clients = clientsMapper.mapToClients(request);
        Clients saveClient;
        try {
            saveClient = clientsRepository.save(clients);
        } catch (Exception e) {
            log.error("Ошибка при сохранении клиента");
            throw new PersistenceException();
        }
        ClientsCreateRsDto clientsCreateRsDto = new ClientsCreateRsDto();
        clientsCreateRsDto.setId(saveClient.getId().toString());
        clientsCreateRsDto.setStatus(saveClient.getStatus().toString());

        return clientsCreateRsDto;
    }

    @Override
    public List<Clients> getAll() {
        return clientsRepository.findAll();
    }

    @Override
    public Clients updateStatus(UUID id, ClientsStatusRqDto clientsStatusRqDto) {
        if (clientsStatusRqDto == null) {
            throw new IllegalArgumentException("Клиент статус dto = null");
        }
        uuidIsNull(id);
        Status clientStatus = clientStatus(clientsStatusRqDto);
        statusIsNull(clientStatus);
        boolean isEquals = Arrays.stream(Status.values()).anyMatch(status -> status.name().equals(clientStatus.name()));
        if (!isEquals) {
            log.error("Некорректный статус");
            throw new InvalidClientsStatusException();

        }
        Clients client = getById(id);
        client.setStatus(clientStatus);
        return clientsRepository.save(client);
    }

    @Override
    public void delete(UUID id) {
        uuidIsNull(id);
        getById(id);
        clientsRepository.deleteById(id);
    }

    public void uuidIsNull(UUID id) {
        if (id == null) {
            log.error("Id равняется null");
            throw new IllegalArgumentException("Id не может быть null");
        }
    }

    public void statusIsNull(Status status) {
        if (status == null) {
            log.error("Status равняется null");
            throw new InvalidClientsStatusException();
        }
    }

    @Override
    public List<ClientsAdminRsDto> getAllAdminClients() {
        return clientsRepository.findAll().stream()
                .map(clientsMapper::mapToClientsAdminRsDto)
                .toList();
    }

    public Status clientStatus(ClientsStatusRqDto clientsStatusRqDto) {
        return clientsStatusRqDto.getStatus();
    }
}
