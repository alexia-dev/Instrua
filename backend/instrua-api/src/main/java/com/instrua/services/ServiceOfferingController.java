package com.instrua.services;

import com.instrua.companies.Company;
import com.instrua.companies.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
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
@RequestMapping("/api/v1/companies/{companyId}/services")
public class ServiceOfferingController {
    private final CompanyService companies;
    private final ServiceOfferingRepository services;
    public ServiceOfferingController(CompanyService companies, ServiceOfferingRepository services) { this.companies = companies; this.services = services; }

    @GetMapping
    public List<ServiceResponse> list(@PathVariable UUID companyId) { companies.requireAccess(companyId); return services.findAllByCompanyIdOrderByNameAsc(companyId).stream().map(ServiceResponse::from).toList(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse create(@PathVariable UUID companyId, @Valid @RequestBody ServiceRequest request) {
        Company company = companies.requireAccess(companyId);
        return ServiceResponse.from(services.save(new ServiceOffering(company, request.name(), request.description(), request.durationMinutes(), request.price())));
    }

    public record ServiceRequest(@NotBlank String name, String description, @Min(1) int durationMinutes, BigDecimal price) { }
    public record ServiceResponse(UUID id, String name, String description, int durationMinutes, BigDecimal price, boolean active) {
        static ServiceResponse from(ServiceOffering offering) { return new ServiceResponse(offering.getId(), offering.getName(), offering.getDescription(), offering.getDurationMinutes(), offering.getPrice(), offering.isActive()); }
    }
}
