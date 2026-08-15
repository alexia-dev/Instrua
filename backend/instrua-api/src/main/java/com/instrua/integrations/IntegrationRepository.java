package com.instrua.integrations;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationRepository extends JpaRepository<Integration, UUID> {
    List<Integration> findAllByCompanyIdOrderByNameAsc(UUID companyId);
}
