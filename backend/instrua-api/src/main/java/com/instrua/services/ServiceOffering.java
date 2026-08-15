package com.instrua.services;

import com.instrua.companies.Company;
import com.instrua.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "service_offerings")
public class ServiceOffering extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    @Column(nullable = false)
    private String name;
    private String description;
    @Column(nullable = false)
    private int durationMinutes;
    private BigDecimal price;
    @Column(nullable = false)
    private boolean active = true;

    protected ServiceOffering() { }
    public ServiceOffering(Company company, String name, String description, int durationMinutes, BigDecimal price) {
        this.company = company; this.name = name; this.description = description; this.durationMinutes = durationMinutes; this.price = price;
    }
    public Company getCompany() { return company; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDurationMinutes() { return durationMinutes; }
    public BigDecimal getPrice() { return price; }
    public boolean isActive() { return active; }
}
