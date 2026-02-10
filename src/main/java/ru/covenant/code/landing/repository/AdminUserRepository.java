package ru.covenant.code.landing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.covenant.code.landing.entity.AdminUser;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, UUID>, JpaSpecificationExecutor<AdminUser> {

    Optional<AdminUser> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(a) FROM AdminUser a WHERE a.active = true")
    long countActiveAdmins();

    @Query("SELECT COUNT(a) FROM AdminUser a WHERE a.active = false")
    long countInactiveAdmins();
}