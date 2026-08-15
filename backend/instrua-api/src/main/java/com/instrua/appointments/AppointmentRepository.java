package com.instrua.appointments;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findAllByCompanyIdOrderByStartsAtAsc(UUID companyId);
    Optional<Appointment> findByIdAndCompanyId(UUID id, UUID companyId);
    boolean existsByCompanyIdAndEmployeeIdAndStartsAtLessThanAndEndsAtGreaterThanAndStatusIn(
            UUID companyId, UUID employeeId, Instant endsAt, Instant startsAt, Collection<AppointmentStatus> statuses);
    long countByCompanyIdAndStartsAtBetween(UUID companyId, Instant from, Instant to);
    long countByCompanyIdAndStatusAndStartsAtBetween(UUID companyId, AppointmentStatus status, Instant from, Instant to);
}
