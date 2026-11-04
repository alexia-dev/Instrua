package com.instrua.appointments;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/appointment-waitlist")
public class AppointmentWaitlistController {
    private final AppointmentWaitlistService waitlist;
    public AppointmentWaitlistController(AppointmentWaitlistService waitlist) { this.waitlist = waitlist; }

    @GetMapping("/services/{serviceId}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','COMPANY_OWNER','COMPANY_ADMIN','RECEPTION','PROFESSIONAL')")
    public List<WaitlistResponse> list(@PathVariable UUID companyId, @PathVariable UUID serviceId) {
        return waitlist.list(companyId, serviceId).stream().map(WaitlistResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','COMPANY_OWNER','COMPANY_ADMIN','RECEPTION','PROFESSIONAL')")
    @ResponseStatus(HttpStatus.CREATED)
    public WaitlistResponse join(@PathVariable UUID companyId, @Valid @RequestBody JoinRequest request) {
        return WaitlistResponse.from(waitlist.join(companyId, request.clientId(), request.serviceId(),
                request.preferredFrom(), request.preferredUntil(), request.priority(), request.notes()));
    }

    @PostMapping("/{entryId}/offer")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','COMPANY_OWNER','COMPANY_ADMIN','RECEPTION')")
    public WaitlistResponse offer(@PathVariable UUID companyId, @PathVariable UUID entryId, @Valid @RequestBody OfferRequest request) {
        return WaitlistResponse.from(waitlist.offer(companyId, entryId, request.appointmentId(), request.expiresAt()));
    }

    @PostMapping("/{entryId}/accept")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','COMPANY_OWNER','COMPANY_ADMIN','RECEPTION')")
    public WaitlistResponse accept(@PathVariable UUID companyId, @PathVariable UUID entryId) {
        return WaitlistResponse.from(waitlist.accept(companyId, entryId));
    }

    @PostMapping("/{entryId}/decline")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','COMPANY_OWNER','COMPANY_ADMIN','RECEPTION')")
    public WaitlistResponse decline(@PathVariable UUID companyId, @PathVariable UUID entryId) {
        return WaitlistResponse.from(waitlist.decline(companyId, entryId));
    }

    public record JoinRequest(@NotNull UUID clientId, @NotNull UUID serviceId, Instant preferredFrom,
            Instant preferredUntil, int priority, String notes) { }
    public record OfferRequest(@NotNull UUID appointmentId, @NotNull Instant expiresAt) { }
    public record WaitlistResponse(UUID id, UUID clientId, String clientName, UUID serviceId, String serviceName,
            Instant preferredFrom, Instant preferredUntil, int priority, WaitlistStatus status,
            UUID offeredAppointmentId, Instant offerExpiresAt, Instant acceptedAt, String notes) {
        static WaitlistResponse from(AppointmentWaitlistEntry e) {
            return new WaitlistResponse(e.getId(), e.getClient().getId(), e.getClient().getName(),
                    e.getServiceOffering().getId(), e.getServiceOffering().getName(), e.getPreferredFrom(),
                    e.getPreferredUntil(), e.getPriority(), e.getStatus(),
                    e.getOfferedAppointment() == null ? null : e.getOfferedAppointment().getId(),
                    e.getOfferExpiresAt(), e.getAcceptedAt(), e.getNotes());
        }
    }
}
