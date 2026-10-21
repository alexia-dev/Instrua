package com.instrua.appointments;

import com.instrua.companies.CompanyService;
import com.instrua.common.exception.BusinessException;
import com.instrua.common.exception.NotFoundException;
import com.instrua.common.security.DomainAccessService;
import com.instrua.users.Role;
import com.instrua.users.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.instrua.companies.Company;
import com.instrua.companies.CompanyRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1/companies/{companyId}")
public class CancellationPolicyController {
    private final CompanyService companies;
    private final CompanyRepository companyRepository;
    private final DomainAccessService access;
    private final JdbcTemplate jdbc;

    public CancellationPolicyController(CompanyService companies, CompanyRepository companyRepository, DomainAccessService access, JdbcTemplate jdbc) {
        this.companies = companies; this.companyRepository = companyRepository; this.access = access; this.jdbc = jdbc;
    }

    @GetMapping("/cancellation-policies")
    public List<Map<String, Object>> list(@PathVariable UUID companyId, @RequestParam(required = false) UUID serviceId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("Estabelecimento não encontrado."));
        if (!company.isActive()) throw new NotFoundException("Estabelecimento indisponível.");
        if (serviceId == null) return jdbc.queryForList("select id, service_offering_id, version, title, policy_text, deposit_amount, cancellation_deadline_minutes, late_cancellation_fee, no_show_fee, currency, effective_from, effective_until from cancellation_policies where company_id = ? and (effective_until is null or effective_until > now()) order by effective_from desc, version desc", companyId);
        return jdbc.queryForList("select id, service_offering_id, version, title, policy_text, deposit_amount, cancellation_deadline_minutes, late_cancellation_fee, no_show_fee, currency, effective_from, effective_until from cancellation_policies where company_id = ? and (service_offering_id = ? or service_offering_id is null) and (effective_until is null or effective_until > now()) order by service_offering_id nulls last, effective_from desc, version desc", companyId, serviceId);
    }

    @PostMapping("/cancellation-policies")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@PathVariable UUID companyId, @Valid @RequestBody PolicyRequest request) {
        companies.requireAccess(companyId);
        access.requireCompanyRole(companyId, Set.of(Role.COMPANY_OWNER, Role.COMPANY_ADMIN));
        if (request.effectiveUntil() != null && !request.effectiveUntil().isAfter(request.effectiveFrom())) throw new BusinessException("A data final deve ser posterior à data inicial.");
        if (request.serviceOfferingId() != null) {
            Integer found = jdbc.queryForObject("select count(*) from service_offerings where id = ? and company_id = ? and active = true", Integer.class, request.serviceOfferingId(), companyId);
            if (found == null || found == 0) throw new NotFoundException("Serviço não encontrado neste estabelecimento.");
        }
        Integer latestVersion = jdbc.queryForObject("select coalesce(max(version), 0) from cancellation_policies where company_id = ? and service_offering_id is not distinct from ?", Integer.class, companyId, request.serviceOfferingId());
        int version = (latestVersion == null ? 0 : latestVersion) + 1;
        UUID id = UUID.randomUUID();
        User user = access.requireUser();
        jdbc.update("insert into cancellation_policies (id, company_id, service_offering_id, version, title, policy_text, deposit_amount, cancellation_deadline_minutes, late_cancellation_fee, no_show_fee, currency, effective_from, effective_until, created_by) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, companyId, request.serviceOfferingId(), version, request.title().trim(), request.policyText().trim(), request.depositAmount(), request.cancellationDeadlineMinutes(), request.lateCancellationFee(), request.noShowFee(), request.currency().toUpperCase(Locale.ROOT), Timestamp.from(request.effectiveFrom()), request.effectiveUntil() == null ? null : Timestamp.from(request.effectiveUntil()), user.getId());
        return jdbc.queryForMap("select id, company_id, service_offering_id, version, title, policy_text, deposit_amount, cancellation_deadline_minutes, late_cancellation_fee, no_show_fee, currency, effective_from, effective_until, created_at from cancellation_policies where id = ?", id);
    }

    public record PolicyRequest(UUID serviceOfferingId, @NotBlank String title, @NotBlank String policyText,
            @DecimalMin("0.00") BigDecimal depositAmount, @Min(0) Integer cancellationDeadlineMinutes,
            @DecimalMin("0.00") BigDecimal lateCancellationFee, @DecimalMin("0.00") BigDecimal noShowFee,
            @NotBlank String currency, @NotNull Instant effectiveFrom, Instant effectiveUntil) {
        public PolicyRequest { if (currency == null || currency.isBlank()) currency = "BRL"; }
    }
    public record AcceptanceRequest(@NotNull UUID policyId, @NotBlank String acceptanceChannel) { }
}
