package com.instrua.appointments;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentWaitlistRepository extends JpaRepository<AppointmentWaitlistEntry, UUID> {
    List<AppointmentWaitlistEntry> findAllByCompanyIdAndServiceOfferingIdAndStatusOrderByPriorityDescCreatedAtAsc(
            UUID companyId, UUID serviceOfferingId, WaitlistStatus status);
    Optional<AppointmentWaitlistEntry> findByIdAndCompanyId(UUID id, UUID companyId);
    List<AppointmentWaitlistEntry> findAllByStatusAndOfferExpiresAtBefore(WaitlistStatus status, Instant now);
}
