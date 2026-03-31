package ru.covenant.code.landing.specification;

import org.junit.jupiter.api.Test;
import ru.covenant.code.landing.dto.client.request.ClientsFilterRqDto;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClientsSpecificationTest {

    private final ClientsSpecification clientsSpecification = new ClientsSpecification();

    @Test
    void withFilter_AllFiltersProvided_ShouldCreateSpecification() {
        // Given
        ClientsFilterRqDto filter = new ClientsFilterRqDto();
        filter.setStartDate(LocalDate.of(2024, 1, 1));
        filter.setEndDate(LocalDate.of(2024, 12, 31));
        filter.setStatuses(List.of(Status.NEW, Status.PROCESSED));
        filter.setPriorities(List.of(Priority.HIGH, Priority.MEDIUM));
        filter.setCourseTypes(List.of(CourseType.BACKEND, CourseType.FRONTEND));
        filter.setSearchQuery("Иван");

        var specification = clientsSpecification.withFilter(filter);


        assertNotNull(specification);
    }

    @Test
    void withFilter_NoFilters_ShouldCreateSpecification() {
        ClientsFilterRqDto filter = new ClientsFilterRqDto();


        var specification = clientsSpecification.withFilter(filter);

        assertNotNull(specification);
    }

    @Test
    void withFilter_NullFilter_ShouldCreateSpecification() {
        var specification = clientsSpecification.withFilter(null);


        assertNotNull(specification);
    }
}