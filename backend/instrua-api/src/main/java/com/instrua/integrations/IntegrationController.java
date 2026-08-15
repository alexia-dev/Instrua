package com.instrua.integrations;

import com.instrua.companies.Company;
import com.instrua.companies.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/integrations")
public class IntegrationController {
    private final CompanyService companies;
    private final IntegrationRepository integrations;
    public IntegrationController(CompanyService companies, IntegrationRepository integrations) { this.companies = companies; this.integrations = integrations; }
    @GetMapping public List<IntegrationResponse> list(@PathVariable UUID companyId) { companies.requireAccess(companyId); return integrations.findAllByCompanyIdOrderByNameAsc(companyId).stream().map(IntegrationResponse::from).toList(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public IntegrationResponse create(@PathVariable UUID companyId, @Valid @RequestBody IntegrationRequest request) {
        Company company = companies.requireAccess(companyId);
        return IntegrationResponse.from(integrations.save(new Integration(company, request.provider(), request.name(), request.externalAccountId(), request.configuration(), request.secretReference())));
    }
    public record IntegrationRequest(@NotNull IntegrationProvider provider, @NotBlank String name, String externalAccountId, String configuration, String secretReference) { }
    public record IntegrationResponse(UUID id, IntegrationProvider provider, String name, boolean active, String externalAccountId, String configuration, boolean hasSecretReference) {
        static IntegrationResponse from(Integration i) { return new IntegrationResponse(i.getId(), i.getProvider(), i.getName(), i.isActive(), i.getExternalAccountId(), i.getConfiguration(), i.getSecretReference() != null && !i.getSecretReference().isBlank()); }
    }
}
