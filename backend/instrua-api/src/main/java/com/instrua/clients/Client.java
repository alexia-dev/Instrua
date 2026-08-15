package com.instrua.clients;

import com.instrua.companies.Company;
import com.instrua.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "clients")
public class Client extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;
    private String email;
    private String phone;
    private String documentNumber;
    private String notes;
    @Column(nullable = false)
    private boolean active = true;

    protected Client() { }

    public Client(Company company, String name, String email, String phone, String documentNumber, String notes) {
        this.company = company;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.documentNumber = documentNumber;
        this.notes = notes;
    }

    public Company getCompany() { return company; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDocumentNumber() { return documentNumber; }
    public String getNotes() { return notes; }
    public boolean isActive() { return active; }
}
