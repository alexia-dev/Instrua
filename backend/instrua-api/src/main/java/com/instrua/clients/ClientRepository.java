package com.instrua.clients;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    List<Client> findAllByCompanyIdOrderByNameAsc(UUID companyId);
    Optional<Client> findByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);
}
