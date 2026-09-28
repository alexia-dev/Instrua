package com.instrua.patients;

import com.instrua.companies.Company;
import com.instrua.common.model.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "patients")
public class Patient extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;
    private String email;
    private String phone;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(length = 4000)
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "user_id")
    private java.util.UUID userId;

    protected Patient() { }

    public Patient(Company company, String name, String email, String phone, String documentNumber, String notes, java.util.UUID userId) {
        this.company = company;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.documentNumber = documentNumber;
        this.notes = notes;
        this.userId = userId;
    }

    public Company getCompany() { return company; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDocumentNumber() { return documentNumber; }
    public String getNotes() { return notes; }
    public boolean isActive() { return active; }
    public java.util.UUID getUserId() { return userId; }
}
