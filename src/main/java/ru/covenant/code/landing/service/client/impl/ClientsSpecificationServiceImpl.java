package ru.covenant.code.landing.service.client.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.service.client.ClientsSpecificationService;
import ru.covenant.code.landing.specification.ClientsSpecification;

@Service
@RequiredArgsConstructor
public class ClientsSpecificationServiceImpl implements ClientsSpecificationService {

    @Override
    public Specification<Clients> toSpecification(ClientsFilterRqDto filter) {
        return ClientsSpecification.withFilter(filter);
    }
}