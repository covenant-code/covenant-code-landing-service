package ru.covenant.code.landing.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.entity.Clients;

import jakarta.persistence.criteria.Predicate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class ClientsSpecification {

    public static Specification<Clients> withFilter(ClientsFilterRqDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                // Фильтр по дате
                if (filter.getStartDate() != null) {
                    OffsetDateTime startDateTime = filter.getStartDate()
                            .atStartOfDay()
                            .atOffset(ZoneOffset.UTC);
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                            root.get("createdAt"), startDateTime));
                }

                if (filter.getEndDate() != null) {
                    OffsetDateTime endDateTime = filter.getEndDate()
                            .plusDays(1)
                            .atStartOfDay()
                            .atOffset(ZoneOffset.UTC);
                    predicates.add(criteriaBuilder.lessThan(
                            root.get("createdAt"), endDateTime));
                }

                // Фильтр по статусам
                if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                    predicates.add(root.get("status").in(filter.getStatuses()));
                }

                // Фильтр по приоритетам
                if (filter.getPriorities() != null && !filter.getPriorities().isEmpty()) {
                    predicates.add(root.get("priority").in(filter.getPriorities()));
                }

                // Фильтр по типам курсов
                if (filter.getCourseTypes() != null && !filter.getCourseTypes().isEmpty()) {
                    predicates.add(root.get("courseType").in(filter.getCourseTypes()));
                }

                // Поиск по тексту
                if (StringUtils.hasText(filter.getSearchQuery())) {
                    String searchPattern = "%" + filter.getSearchQuery().toLowerCase() + "%";
                    Predicate namePredicate = criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")), searchPattern);
                    Predicate emailPredicate = criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("email")), searchPattern);
                    Predicate phonePredicate = criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("phone")), searchPattern);
                    predicates.add(criteriaBuilder.or(namePredicate, emailPredicate, phonePredicate));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}