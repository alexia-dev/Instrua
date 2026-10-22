package com.instrua.companies;

import com.instrua.users.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {
    private final CompanyService companies;
    private final CurrentUser currentUser;

    public CompanyController(CompanyService companies, CurrentUser currentUser) {
        this.companies = companies;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<CompanyResponse> list() { return companies.accessible().stream().map(CompanyResponse::from).toList(); }

    @GetMapping("/{id}")
    public CompanyResponse get(@PathVariable UUID id) { return CompanyResponse.from(companies.requireAccess(id)); }

    @PatchMapping("/{id}/vacancy-auction")
    public VacancyAuctionResponse configureVacancyAuction(@PathVariable UUID id, @Valid @RequestBody VacancyAuctionRequest request) {
        Company company = companies.requireAccess(id);
        company.configureVacancyAuction(request.enabled(), request.allowDiscount(), request.defaultExpiryMinutes(), request.defaultReservationMinutes());
        return new VacancyAuctionResponse(company.isVacancyAuctionEnabled(), company.isVacancyAllowDiscount(), company.getVacancyDefaultExpiryMinutes(), company.getVacancyDefaultReservationMinutes());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse create(@Valid @RequestBody CompanyRequest request) {
        return CompanyResponse.from(companies.create(request.name(), request.slug(), request.email(), request.phone(), request.timezone(), currentUser.get()));
    }

    public record CompanyRequest(@NotBlank String name, @NotBlank String slug, @Email String email, String phone, String timezone) { }
    public record VacancyAuctionRequest(boolean enabled, boolean allowDiscount, int defaultExpiryMinutes, int defaultReservationMinutes) { }
    public record VacancyAuctionResponse(boolean enabled, boolean allowDiscount, int defaultExpiryMinutes, int defaultReservationMinutes) { }
    public record CompanyResponse(UUID id, String name, String slug, String email, String phone, String timezone, boolean active, String niche,
                                  boolean vacancyAuctionEnabled, boolean vacancyAllowDiscount, int vacancyDefaultExpiryMinutes, int vacancyDefaultReservationMinutes) {
        public static CompanyResponse from(Company company) { return new CompanyResponse(company.getId(), company.getName(), company.getSlug(), company.getEmail(), company.getPhone(), company.getTimezone(), company.isActive(), company.getNiche(), company.isVacancyAuctionEnabled(), company.isVacancyAllowDiscount(), company.getVacancyDefaultExpiryMinutes(), company.getVacancyDefaultReservationMinutes()); }
    }
}
