package com.instrua.modules;

import com.instrua.common.tenant.TenantAccessService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModuleCatalogService {
    private final PlatformModuleRepository modules;
    private final CompanyModuleSubscriptionRepository subscriptions;
    private final TenantAccessService tenantAccess;

    public ModuleCatalogService(PlatformModuleRepository modules, CompanyModuleSubscriptionRepository subscriptions,
                                 TenantAccessService tenantAccess) {
        this.modules = modules;
        this.subscriptions = subscriptions;
        this.tenantAccess = tenantAccess;
    }

    @Transactional(readOnly = true)
    public List<ModuleResponse> catalog() {
        return modules.findAllByActiveTrueOrderByDisplayOrderAsc().stream().map(ModuleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CompanySubscriptionResponse> subscriptions(UUID companyId) {
        tenantAccess.requireAccess(companyId);
        return subscriptions.findAllByCompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(CompanySubscriptionResponse::from).toList();
    }

    public record ModuleResponse(String code, String name, String description, String category,
                                 boolean required, boolean active, int displayOrder) {
        static ModuleResponse from(PlatformModule module) {
            return new ModuleResponse(module.getCode(), module.getName(), module.getDescription(),
                    module.getCategory(), module.isRequiredModule(), module.isActive(), module.getDisplayOrder());
        }
    }

    public record CompanySubscriptionResponse(UUID id, String moduleCode, String moduleName, ModuleStatus status,
                                               Instant startedAt, Instant currentPeriodStart, Instant currentPeriodEnd,
                                               Instant trialEndsAt, Instant canceledAt) {
        static CompanySubscriptionResponse from(CompanyModuleSubscription subscription) {
            return new CompanySubscriptionResponse(subscription.getId(), subscription.getModule().getCode(),
                    subscription.getModule().getName(), subscription.getStatus(), subscription.getStartedAt(),
                    subscription.getCurrentPeriodStart(), subscription.getCurrentPeriodEnd(),
                    subscription.getTrialEndsAt(), subscription.getCanceledAt());
        }
    }
}
