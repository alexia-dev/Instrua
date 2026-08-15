package com.instrua.notifications;

import com.instrua.appointments.Appointment;
import com.instrua.appointments.AppointmentRepository;
import com.instrua.companies.Company;
import com.instrua.companies.CompanyService;
import com.instrua.common.exception.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/notifications")
@Transactional
public class NotificationController {
    private final CompanyService companies;
    private final NotificationRepository notifications;
    private final AppointmentRepository appointments;
    public NotificationController(CompanyService companies, NotificationRepository notifications, AppointmentRepository appointments) { this.companies = companies; this.notifications = notifications; this.appointments = appointments; }

    @GetMapping
    public List<NotificationResponse> list(@PathVariable UUID companyId) { companies.requireAccess(companyId); return notifications.findAllByCompanyIdOrderByCreatedAtDesc(companyId).stream().map(NotificationResponse::from).toList(); }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse queue(@PathVariable UUID companyId, @Valid @RequestBody NotificationRequest request) {
        Company company = companies.requireAccess(companyId);
        Appointment appointment = request.appointmentId() == null ? null : appointments.findByIdAndCompanyId(request.appointmentId(), companyId).orElseThrow(() -> new NotFoundException("Agendamento não encontrado"));
        return NotificationResponse.from(notifications.save(new Notification(company, appointment, request.channel(), request.recipient(), request.subject(), request.body(), request.scheduledFor())));
    }
    public record NotificationRequest(UUID appointmentId, @NotNull NotificationChannel channel, @NotBlank String recipient, String subject, @NotBlank String body, Instant scheduledFor) { }
    public record NotificationResponse(UUID id, UUID appointmentId, NotificationChannel channel, NotificationStatus status, String recipient, String subject, String body, Instant scheduledFor, Instant sentAt, String providerMessageId, String failureReason) {
        static NotificationResponse from(Notification n) { return new NotificationResponse(n.getId(), n.getAppointment() == null ? null : n.getAppointment().getId(), n.getChannel(), n.getStatus(), n.getRecipient(), n.getSubject(), n.getBody(), n.getScheduledFor(), n.getSentAt(), n.getProviderMessageId(), n.getFailureReason()); }
    }
}
