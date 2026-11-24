package com.instrua.employees;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    List<Employee> findAllByCompanyIdOrderByNameAsc(UUID companyId);
    Optional<Employee> findByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);
    boolean existsByCompanyIdAndAccountId(UUID companyId, UUID accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Employee e where e.id = :employeeId and e.company.id = :companyId and e.active = true")
    Optional<Employee> lockActiveForBooking(@Param("employeeId") UUID employeeId, @Param("companyId") UUID companyId);
}
