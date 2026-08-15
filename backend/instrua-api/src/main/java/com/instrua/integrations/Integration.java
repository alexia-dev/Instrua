package com.instrua.integrations;

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

@Entity
@Table(name = "integrations")
public class Integration extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private IntegrationProvider provider;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private boolean active = true;
    private String externalAccountId;
    @Column(length = 4000) private String configuration;
    private String secretReference;

    protected Integration() { }
    public Integration(Company company, IntegrationProvider provider, String name, String externalAccountId, String configuration, String secretReference) {
        this.company = company; this.provider = provider; this.name = name; this.externalAccountId = externalAccountId; this.configuration = configuration; this.secretReference = secretReference;
    }
    public IntegrationProvider getProvider() { return provider; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
    public String getExternalAccountId() { return externalAccountId; }
    public String getConfiguration() { return configuration; }
    public String getSecretReference() { return secretReference; }
}
