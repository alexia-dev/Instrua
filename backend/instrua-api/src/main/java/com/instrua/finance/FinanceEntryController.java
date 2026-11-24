package com.instrua.finance;

import com.instrua.companies.CompanyService;
import com.instrua.common.security.DomainAccessService;
import com.instrua.users.Role;
import com.instrua.users.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.*;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/finance/entries")
public class FinanceEntryController {
    private final CompanyService companies;
    private final DomainAccessService access;
    private final JdbcTemplate jdbc;
    public FinanceEntryController(CompanyService companies, DomainAccessService access, JdbcTemplate jdbc) { this.companies = companies; this.access = access; this.jdbc = jdbc; }

    @GetMapping
    public List<Map<String, Object>> list(@PathVariable UUID companyId, @RequestParam(required = false) String status) {
        companies.requireAccess(companyId); access.requireBillingRole(companyId);
        if (status == null || status.isBlank()) return jdbc.queryForList("select id, entry_type, category, description, amount, currency, due_at, settled_at, status, source_type, source_id, created_at, updated_at from finance_entries where company_id = ? order by due_at nulls last, created_at desc limit 500", companyId);
        return jdbc.queryForList("select id, entry_type, category, description, amount, currency, due_at, settled_at, status, source_type, source_id, created_at, updated_at from finance_entries where company_id = ? and status = ? order by due_at nulls last, created_at desc limit 500", companyId, status.toUpperCase(Locale.ROOT));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@PathVariable UUID companyId, @Valid @RequestBody EntryRequest request) {
        companies.requireAccess(companyId); access.requireBillingRole(companyId);
        User user = access.requireUser(); UUID id = UUID.randomUUID();
        jdbc.update("insert into finance_entries (id, company_id, entry_type, category, description, amount, currency, due_at, status, source_type, source_id, created_by) values (?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, ?, ?)",
                id, companyId, request.entryType().name(), request.category().trim(), request.description().trim(), request.amount(), request.currency().toUpperCase(Locale.ROOT), request.dueAt() == null ? null : Date.valueOf(request.dueAt()), request.sourceType(), request.sourceId(), user.getId());
        return jdbc.queryForMap("select id, company_id, entry_type, category, description, amount, currency, due_at, settled_at, status, source_type, source_id, created_by, created_at, updated_at from finance_entries where id = ? and company_id = ?", id, companyId);
    }

    public enum EntryType { INCOME, EXPENSE, PAYOUT, REFUND, TAX }
    public record EntryRequest(@NotNull EntryType entryType, @NotBlank String category, @NotBlank String description,
            @NotNull @DecimalMin("0.00") BigDecimal amount, @NotBlank String currency, java.time.LocalDate dueAt, String sourceType, UUID sourceId) {
        public EntryRequest { if (currency == null || currency.isBlank()) currency = "BRL"; }
    }
}
