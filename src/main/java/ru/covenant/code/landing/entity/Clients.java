package ru.covenant.code.landing.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import ru.covenant.code.landing.entity.enumerated.CourseType;
import ru.covenant.code.landing.entity.enumerated.Priority;
import ru.covenant.code.landing.entity.enumerated.Status;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "clients_db")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Clients {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String email;

    @Column(nullable = true)
    String phone;

    @Column(columnDefinition = "TEXT")
    String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    CourseType courseType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status = Status.NEW;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Priority priority = Priority.MEDIUM;

    @Builder.Default
    @Column
    String source = "Лендинг";

    @CreationTimestamp
    @Column(name = "created_at",
            updatable = false, nullable = false)
    OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",
            nullable = false)
    OffsetDateTime updatedAt;

    @Column( name = "processed_by")
    String processedBy;

    @Column(name = "processed_at")
    OffsetDateTime processedAt;
}
