package ru.covenant.code.landing.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.entity.Clients;


import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Component
public class ClientsSpecification {

    public Specification<Clients> withFilter(ClientsFilterRqDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (filter.getStartDate() != null) {
                OffsetDateTime startOfDay = filter.getStartDate()
                        .atStartOfDay()
                        .atOffset(ZoneOffset.UTC);
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startOfDay));
            }

            if (filter.getEndDate() != null) {
                OffsetDateTime endOfDay = filter.getEndDate()
                        .atTime(LocalTime.MAX)
                        .atOffset(ZoneOffset.UTC);
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endOfDay));
            }

            if (!CollectionUtils.isEmpty(filter.getStatuses())) {
                predicates.add(root.get("status").in(filter.getStatuses()));
            }

            if (!CollectionUtils.isEmpty(filter.getPriorities())) {
                predicates.add(root.get("priority").in(filter.getPriorities()));
            }

            if (!CollectionUtils.isEmpty(filter.getCourseTypes())) {
                predicates.add(root.get("courseType").in(filter.getCourseTypes()));
            }


            if (StringUtils.hasText(filter.getSearchQuery())) {
                String searchPattern = "%" + filter.getSearchQuery().toLowerCase() + "%";

                Predicate namePredicate = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), searchPattern);
                Predicate phonePredicate = cb.like(cb.lower(root.get("phone")), searchPattern);

                predicates.add(cb.or(namePredicate, emailPredicate, phonePredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}