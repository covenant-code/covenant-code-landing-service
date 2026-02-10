package ru.covenant.code.landing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.covenant.code.landing.entity.Clients;
import ru.covenant.code.landing.entity.enumerated.Status;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClientsRepository extends JpaRepository<Clients, UUID>, JpaSpecificationExecutor<Clients> {

    List<Clients> findByStatus(Status status, Sort sort);

    long countByStatus(Status status);

    long countByCreatedAtAfter(OffsetDateTime date);

    long countByCourseType(CourseType courseType);

    long countByPriority(Priority priority);

    @Query("SELECT COUNT(c) FROM Clients c WHERE c.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(@Param("start") OffsetDateTime start,
                                 @Param("end") OffsetDateTime end);

    boolean existsByEmail(String email);
}