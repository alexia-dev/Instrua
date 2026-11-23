package com.instrua.catalog;

import com.instrua.companies.CompanyRepository;
import com.instrua.employees.Employee;
import com.instrua.employees.EmployeeRepository;
import com.instrua.services.ServiceOffering;
import com.instrua.services.ServiceOfferingRepository;
import com.instrua.users.Role;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public, non-sensitive discovery; never returns clients, patient records or accounts. */
@RestController
@RequestMapping("/api/v1/catalog")
public class PublicCatalogController {
    private final CompanyRepository companies;
    private final ServiceOfferingRepository services;
    private final EmployeeRepository employees;

    public PublicCatalogController(CompanyRepository companies, ServiceOfferingRepository services, EmployeeRepository employees) {
        this.companies = companies;
        this.services = services;
        this.employees = employees;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<CompanyItem> catalog() {
        return companies.findAll().stream().filter(c -> c.isActive())
                .sorted(Comparator.comparing(c -> c.getName().toLowerCase()))
                .map(c -> new CompanyItem(c.getId(), c.getName(), c.getSlug(), c.getTimezone(),
                        services.findAllByCompanyIdOrderByNameAsc(c.getId()).stream()
                                .filter(ServiceOffering::isActive)
                                .map(s -> new ServiceItem(s.getId(), s.getName(), s.getDescription(), s.getDurationMinutes(), s.getPrice())).toList(),
                        employees.findAllByCompanyIdOrderByNameAsc(c.getId()).stream()
                                .filter(e -> e.isActive() && e.getAccessRole() == Role.CLINICAL)
                                .map(e -> new ProfessionalItem(e.getId(), e.getName(), e.getTitle())).toList())).toList();
    }

    public record CompanyItem(UUID id, String name, String slug, String timezone, List<ServiceItem> services, List<ProfessionalItem> professionals) { }
    public record ServiceItem(UUID id, String name, String description, int durationMinutes, BigDecimal price) { }
    public record ProfessionalItem(UUID id, String name, String title) { }
}
