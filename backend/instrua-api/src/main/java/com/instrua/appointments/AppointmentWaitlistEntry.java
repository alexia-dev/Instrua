package com.instrua.appointments;

import com.instrua.clients.Client;
import com.instrua.companies.Company;
import com.instrua.common.model.BaseEntity;
import com.instrua.services.ServiceOffering;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "appointment_waitlist_entries")
public class AppointmentWaitlistEntry extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "service_offering_id", nullable = false)
    private ServiceOffering serviceOffering;
    private Instant preferredFrom;
    private Instant preferredUntil;
    @Column(nullable = false) private int priority;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private WaitlistStatus status = WaitlistStatus.WAITING;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "offered_appointment_id")
    private Appointment offeredAppointment;
    private Instant offerExpiresAt;
    private Instant acceptedAt;
    @Column(length = 2000) private String notes;

    protected AppointmentWaitlistEntry() { }

    public AppointmentWaitlistEntry(Company company, Client client, ServiceOffering serviceOffering,
            Instant preferredFrom, Instant preferredUntil, int priority, String notes) {
        this.company = company; this.client = client; this.serviceOffering = serviceOffering;
        this.preferredFrom = preferredFrom; this.preferredUntil = preferredUntil;
        this.priority = priority; this.notes = notes;
    }

    public Company getCompany() { return company; }
    public Client getClient() { return client; }
    public ServiceOffering getServiceOffering() { return serviceOffering; }
    public Instant getPreferredFrom() { return preferredFrom; }
    public Instant getPreferredUntil() { return preferredUntil; }
    public int getPriority() { return priority; }
    public WaitlistStatus getStatus() { return status; }
    public Appointment getOfferedAppointment() { return offeredAppointment; }
    public Instant getOfferExpiresAt() { return offerExpiresAt; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public String getNotes() { return notes; }

    public void offer(Appointment appointment, Instant expiresAt) {
        if (status != WaitlistStatus.WAITING) throw new IllegalStateException("Entrada não está aguardando");
        offeredAppointment = appointment; offerExpiresAt = expiresAt; status = WaitlistStatus.OFFERED;
    }
    public void accept(Instant now) {
        if (status != WaitlistStatus.OFFERED) throw new IllegalStateException("Não existe oferta ativa");
        if (offerExpiresAt != null && now.isAfter(offerExpiresAt)) {
            status = WaitlistStatus.EXPIRED;
            throw new IllegalStateException("A oferta da vaga expirou");
        }
        acceptedAt = now; status = WaitlistStatus.ACCEPTED;
    }
    public void decline() {
        if (status != WaitlistStatus.OFFERED) throw new IllegalStateException("Não existe oferta ativa");
        status = WaitlistStatus.DECLINED;
    }
    public void expire() { if (status == WaitlistStatus.OFFERED) status = WaitlistStatus.EXPIRED; }
}
