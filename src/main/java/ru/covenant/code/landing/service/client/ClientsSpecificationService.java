package ru.covenant.code.landing.service.client;

import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.entity.Clients;
import org.springframework.data.jpa.domain.Specification;

public interface ClientsSpecificationService {
    Specification<Clients> toSpecification(ClientsFilterRqDto filter);
}