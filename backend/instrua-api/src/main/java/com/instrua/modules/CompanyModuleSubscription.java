package com.instrua.modules;

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
@Table(name = "company_module_subscriptions")
public class CompanyModuleSubscription extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_code", referencedColumnName = "code", nullable = false)
    private PlatformModule module;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ModuleStatus status;

    @Column(name = "started_at") private Instant startedAt;
    @Column(name = "current_period_start") private Instant currentPeriodStart;
    @Column(name = "current_period_end") private Instant currentPeriodEnd;
    @Column(name = "trial_ends_at") private Instant trialEndsAt;
    @Column(name = "canceled_at") private Instant canceledAt;
    @Column(name = "external_subscription_id", length = 180) private String externalSubscriptionId;

    protected CompanyModuleSubscription() { }
    public Company getCompany() { return company; }
    public PlatformModule getModule() { return module; }
    public ModuleStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCurrentPeriodStart() { return currentPeriodStart; }
    public Instant getCurrentPeriodEnd() { return currentPeriodEnd; }
    public Instant getTrialEndsAt() { return trialEndsAt; }
    public Instant getCanceledAt() { return canceledAt; }
}
