package com.instrua.appointments;

import com.instrua.clients.Client;
import com.instrua.companies.Company;
import com.instrua.common.model.BaseEntity;
import com.instrua.employees.Employee;
import com.instrua.services.ServiceOffering;
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
@Table(name = "appointments")
public class Appointment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "employee_id")
    private Employee employee;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "service_offering_id", nullable = false)
    private ServiceOffering serviceOffering;
    @Column(nullable = false) private Instant startsAt;
    @Column(nullable = false) private Instant endsAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AppointmentStatus status = AppointmentStatus.SCHEDULED;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ConfirmationStatus confirmationStatus = ConfirmationStatus.PENDING;
    @Column(length = 2000) private String notes;
    private String cancellationReason;

    protected Appointment() { }
    public Appointment(Company company, Client client, Employee employee, ServiceOffering serviceOffering, Instant startsAt, Instant endsAt, String notes) {
        this.company = company; this.client = client; this.employee = employee; this.serviceOffering = serviceOffering;
        this.startsAt = startsAt; this.endsAt = endsAt; this.notes = notes;
    }
    public Company getCompany() { return company; }
    public Client getClient() { return client; }
    public Employee getEmployee() { return employee; }
    public ServiceOffering getServiceOffering() { return serviceOffering; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public AppointmentStatus getStatus() { return status; }
    public ConfirmationStatus getConfirmationStatus() { return confirmationStatus; }
    public String getNotes() { return notes; }
    public String getCancellationReason() { return cancellationReason; }
    public void changeStatus(AppointmentStatus status, String cancellationReason) {
        this.status = status;
        if (status == AppointmentStatus.CONFIRMED) this.confirmationStatus = ConfirmationStatus.CONFIRMED;
        if (status == AppointmentStatus.CANCELLED) { this.confirmationStatus = ConfirmationStatus.DECLINED; this.cancellationReason = cancellationReason; }
    }
}
