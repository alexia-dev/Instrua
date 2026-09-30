package com.instrua.journey;

import com.instrua.appointments.AppointmentRepository;
import com.instrua.common.tenant.TenantAccessService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/journey")
public class JourneyController {
    private final TenantAccessService tenants;
    private final AppointmentRepository appointments;

    public JourneyController(TenantAccessService tenants, AppointmentRepository appointments) {
        this.tenants = tenants;
        this.appointments = appointments;
    }

    @GetMapping("/summary")
    public JourneySummary summary(@PathVariable UUID companyId) {
        tenants.requireAccess(companyId);
        var all = appointments.findAllByCompanyIdOrderByStartsAtAsc(companyId);
        long scheduled = all.size();
        long confirmed = all.stream().filter(a -> a.getConfirmationStatus().name().equals("CONFIRMED")).count();
        long pending = all.stream().filter(a -> a.getConfirmationStatus().name().equals("PENDING")).count();
        return new JourneySummary(scheduled, confirmed, pending);
    }

    public record JourneySummary(long scheduled, long confirmed, long pending) {}
}
