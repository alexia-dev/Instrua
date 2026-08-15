package com.instrua.notifications;

import com.instrua.appointments.Appointment;
import com.instrua.companies.Company;
import com.instrua.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "appointment_id")
    private Appointment appointment;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private NotificationChannel channel;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private NotificationStatus status = NotificationStatus.PENDING;
    @Column(nullable = false) private String recipient;
    private String subject;
    @Column(nullable = false, length = 10000) private String body;
    private Instant scheduledFor;
    private Instant sentAt;
    private String providerMessageId;
    private String failureReason;

    protected Notification() { }
    public Notification(Company company, Appointment appointment, NotificationChannel channel, String recipient, String subject, String body, Instant scheduledFor) {
        this.company = company; this.appointment = appointment; this.channel = channel; this.recipient = recipient; this.subject = subject; this.body = body; this.scheduledFor = scheduledFor;
        this.status = scheduledFor == null ? NotificationStatus.PENDING : NotificationStatus.SCHEDULED;
    }
    public Appointment getAppointment() { return appointment; }
    public NotificationChannel getChannel() { return channel; }
    public NotificationStatus getStatus() { return status; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public Instant getScheduledFor() { return scheduledFor; }
    public Instant getSentAt() { return sentAt; }
    public String getProviderMessageId() { return providerMessageId; }
    public String getFailureReason() { return failureReason; }
}
