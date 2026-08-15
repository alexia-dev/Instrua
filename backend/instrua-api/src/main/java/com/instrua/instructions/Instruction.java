package com.instrua.instructions;

import com.instrua.companies.Company;
import com.instrua.common.model.BaseEntity;
import com.instrua.services.ServiceOffering;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "instructions")
public class Instruction extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "service_offering_id")
    private ServiceOffering serviceOffering;
    @Column(nullable = false) private String title;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private InstructionContentType contentType;
    @Column(nullable = false, length = 10000) private String content;
    @Column(nullable = false) private int displayOrder = 0;
    @Column(nullable = false) private boolean active = true;

    protected Instruction() { }
    public Instruction(Company company, ServiceOffering serviceOffering, String title, InstructionContentType contentType, String content, int displayOrder) {
        this.company = company; this.serviceOffering = serviceOffering; this.title = title; this.contentType = contentType; this.content = content; this.displayOrder = displayOrder;
    }
    public ServiceOffering getServiceOffering() { return serviceOffering; }
    public String getTitle() { return title; }
    public InstructionContentType getContentType() { return contentType; }
    public String getContent() { return content; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isActive() { return active; }
}
