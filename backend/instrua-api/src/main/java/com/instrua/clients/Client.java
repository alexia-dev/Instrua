package com.instrua.clients;

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
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "clients")
public class Client extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    @Column(nullable = false) private String name;
    private String email;
    private String phone;
    private String documentNumber;
    private String notes;
    @Enumerated(EnumType.STRING) @Column(name = "client_type", nullable = false) private ClientType clientType = ClientType.PERSON;
    private String legalName;
    private String tradeName;
    private String contactName;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "profile_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> profileData = new LinkedHashMap<>();
    @Column(nullable = false) private boolean active = true;

    protected Client() { }
    public Client(Company company, String name, String email, String phone, String documentNumber, String notes) {
        this(company, name, email, phone, documentNumber, notes, ClientType.PERSON, null, null, null, Map.of());
    }
    public Client(Company company, String name, String email, String phone, String documentNumber, String notes,
                  ClientType clientType, String legalName, String tradeName, String contactName, Map<String, Object> profileData) {
        this.company = company; this.name = name; this.email = email; this.phone = phone; this.documentNumber = documentNumber; this.notes = notes;
        this.clientType = clientType == null ? ClientType.PERSON : clientType; this.legalName = legalName; this.tradeName = tradeName;
        this.contactName = contactName; this.profileData = profileData == null ? new LinkedHashMap<>() : new LinkedHashMap<>(profileData);
    }
    public Company getCompany() { return company; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDocumentNumber() { return documentNumber; }
    public String getNotes() { return notes; }
    public ClientType getClientType() { return clientType; }
    public String getLegalName() { return legalName; }
    public String getTradeName() { return tradeName; }
    public String getContactName() { return contactName; }
    public Map<String, Object> getProfileData() { return Map.copyOf(profileData); }
    public boolean isActive() { return active; }
}
