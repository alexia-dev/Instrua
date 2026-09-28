package com.instrua.patients;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    List<Patient> findAllByCompanyIdOrderByNameAsc(UUID companyId);
    Optional<Patient> findByIdAndCompanyIdAndActiveTrue(UUID id, UUID companyId);
}
