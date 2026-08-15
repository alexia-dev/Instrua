package com.instrua.companies;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    boolean existsBySlugIgnoreCase(String slug);
    List<Company> findAllByOwnerId(UUID ownerId);
    @Query("select distinct e.company from Employee e where e.account.id = :userId and e.active = true")
    List<Company> findAllAccessibleByUserId(@Param("userId") UUID userId);
    @Query("select case when count(c) > 0 then true else false end from Company c where c.id = :companyId and c.owner.id = :userId")
    boolean isOwner(@Param("companyId") UUID companyId, @Param("userId") UUID userId);
    Optional<Company> findBySlugIgnoreCase(String slug);
}
