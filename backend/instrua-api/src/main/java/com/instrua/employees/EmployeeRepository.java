package com.instrua.employees;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    List<Employee> findAllByCompanyIdOrderByNameAsc(UUID companyId);
    Optional<Employee> findByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);
    boolean existsByCompanyIdAndAccountId(UUID companyId, UUID accountId);
}
