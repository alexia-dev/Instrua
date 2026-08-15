package com.instrua.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, UUID> {
    List<ServiceOffering> findAllByCompanyIdOrderByNameAsc(UUID companyId);
    Optional<ServiceOffering> findByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);
}
