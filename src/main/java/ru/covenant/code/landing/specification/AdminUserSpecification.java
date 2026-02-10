package ru.covenant.code.landing.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.covenant.code.landing.dto.admin.request.AdminFilterRqDto;
import ru.covenant.code.landing.entity.AdminUser;
import ru.covenant.code.landing.entity.enumerated.AdminRole;

import java.util.ArrayList;
import java.util.List;

public class AdminUserSpecification {

    public static Specification<AdminUser> filter(AdminFilterRqDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), searchPattern)
                ));
            }

            if (filter.getRole() != null && !filter.getRole().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("role"),
                        AdminRole.valueOf(filter.getRole().toUpperCase())));
            }

            if (filter.getActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), filter.getActive()));
            }

            if (filter.getDepartment() != null && !filter.getDepartment().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("department"), filter.getDepartment()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}