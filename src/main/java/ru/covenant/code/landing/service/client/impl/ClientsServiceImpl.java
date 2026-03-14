package ru.covenant.code.landing.service.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.dto.client.response.ClientsAdminRsDto;
import ru.covenant.code.landing.exceptions.ExceptionFactory;
import ru.covenant.code.landing.mapper.ClientsMapper;
import ru.covenant.code.landing.repository.ClientsRepository;
import ru.covenant.code.landing.service.client.ClientsService;
import ru.covenant.code.landing.specification.ClientsSpecification;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientsServiceImpl implements ClientsService {

    private final ClientsRepository clientsRepository;
    private final ClientsSpecification clientsSpecification;
    private final ClientsMapper clientsMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ClientsAdminRsDto> getAllClients(ClientsFilterRqDto filter) {
        log.info("Запрос списка клиентов с фильтром: startDate={}, endDate={}, statuses={}, priorities={}, courseTypes={}, searchQuery={}",
                filter.getStartDate(), filter.getEndDate(), filter.getStatuses(),
                filter.getPriorities(), filter.getCourseTypes(), filter.getSearchQuery());

        try {
            var specification = clientsSpecification.withFilter(filter);
            var sort = Sort.by(Sort.Direction.DESC, "createdAt");

            var clients = clientsRepository.findAll(specification, sort);
            log.debug("Найдено {} клиентов", clients.size());

            return clientsMapper.toAdminResponseList(clients);

        } catch (Exception e) {
            log.error("Ошибка при получении списка клиентов: {}", e.getMessage(), e);
            throw ExceptionFactory.internalError("Ошибка при получении списка клиентов");
        }
    }
}