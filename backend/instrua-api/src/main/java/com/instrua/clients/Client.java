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
    private String rg;
    private java.time.LocalDate birthDate;
    private String postalCode;
    private String street;
    private String streetNumber;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private java.util.UUID linkedUserId;
    @Column(length = 4000)
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
    public String getRg() { return rg; }
    public java.time.LocalDate getBirthDate() { return birthDate; }
    public String getPostalCode() { return postalCode; }
    public String getStreet() { return street; }
    public String getStreetNumber() { return streetNumber; }
    public String getComplement() { return complement; }
    public String getNeighborhood() { return neighborhood; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public java.util.UUID getLinkedUserId() { return linkedUserId; }
    public void updateContact(String name,String email,String phone,String cpf,String notes) {
        this.name=name.trim();this.email=email;this.phone=phone;this.documentNumber=cpf;this.notes=notes;
    }
    public void fillAddress(String rg,java.time.LocalDate birthDate,String postalCode,String street,String streetNumber,
                            String complement,String neighborhood,String city,String state) {
        this.rg=rg;this.birthDate=birthDate;this.postalCode=postalCode;this.street=street;
        this.streetNumber=streetNumber;this.complement=complement;this.neighborhood=neighborhood;
        this.city=city;this.state=state;
    }
    public void linkAccount(java.util.UUID userId){this.linkedUserId=userId;}
    public String getNotes() { return notes; }
    public boolean isActive() { return active; }
}
