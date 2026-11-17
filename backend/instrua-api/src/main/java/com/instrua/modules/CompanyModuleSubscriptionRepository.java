package com.instrua.modules;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyModuleSubscriptionRepository extends JpaRepository<CompanyModuleSubscription, UUID> {
    List<CompanyModuleSubscription> findAllByCompanyIdOrderByCreatedAtDesc(UUID companyId);
}
