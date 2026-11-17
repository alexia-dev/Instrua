package com.instrua.modules;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ModuleController {
    private final ModuleCatalogService moduleCatalog;

    public ModuleController(ModuleCatalogService moduleCatalog) { this.moduleCatalog = moduleCatalog; }

    @GetMapping("/modules/catalog")
    public List<ModuleCatalogService.ModuleResponse> catalog() { return moduleCatalog.catalog(); }

    @GetMapping("/companies/{companyId}/modules")
    public List<ModuleCatalogService.CompanySubscriptionResponse> companyModules(@PathVariable UUID companyId) {
        return moduleCatalog.subscriptions(companyId);
    }
}
