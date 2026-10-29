package com.instrua.clients;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    List<Client> findAllByCompanyIdOrderByNameAsc(UUID companyId);
    Optional<Client> findByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Client c where c.id = :id and c.company.id = :companyId and c.active = true")
    Optional<Client> findForAppointmentWrite(@Param("id") UUID id, @Param("companyId") UUID companyId);
}
