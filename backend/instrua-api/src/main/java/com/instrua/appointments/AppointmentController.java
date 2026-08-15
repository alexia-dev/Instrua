package com.instrua.appointments;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/appointments")
@Transactional
public class AppointmentController {
    private final AppointmentService appointments;
    public AppointmentController(AppointmentService appointments) { this.appointments = appointments; }

    @GetMapping
    public List<AppointmentResponse> list(@PathVariable UUID companyId) { return appointments.list(companyId).stream().map(AppointmentResponse::from).toList(); }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse create(@PathVariable UUID companyId, @Valid @RequestBody AppointmentRequest request) {
        return AppointmentResponse.from(appointments.create(companyId, request.clientId(), request.employeeId(), request.serviceId(), request.startsAt(), request.notes()));
    }
    @PatchMapping("/{appointmentId}/status")
    public AppointmentResponse changeStatus(@PathVariable UUID companyId, @PathVariable UUID appointmentId, @Valid @RequestBody StatusRequest request) {
        return AppointmentResponse.from(appointments.changeStatus(companyId, appointmentId, request.status(), request.reason()));
    }

    public record AppointmentRequest(@NotNull UUID clientId, UUID employeeId, @NotNull UUID serviceId, @NotNull @Future Instant startsAt, String notes) { }
    public record StatusRequest(@NotNull AppointmentStatus status, String reason) { }
    public record AppointmentResponse(UUID id, UUID clientId, String clientName, UUID employeeId, String employeeName, UUID serviceId, String serviceName,
                                      Instant startsAt, Instant endsAt, AppointmentStatus status, ConfirmationStatus confirmationStatus, String notes, String cancellationReason) {
        static AppointmentResponse from(Appointment a) { return new AppointmentResponse(a.getId(), a.getClient().getId(), a.getClient().getName(), a.getEmployee() == null ? null : a.getEmployee().getId(), a.getEmployee() == null ? null : a.getEmployee().getName(), a.getServiceOffering().getId(), a.getServiceOffering().getName(), a.getStartsAt(), a.getEndsAt(), a.getStatus(), a.getConfirmationStatus(), a.getNotes(), a.getCancellationReason()); }
    }
}
