package com.instrua.vacancy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.instrua.companies.CompanyService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/vacancy-opportunities")
public class VacancyOpportunityController {
    private final JdbcTemplate jdbc;
    private final CompanyService companies;

    public VacancyOpportunityController(JdbcTemplate jdbc, CompanyService companies) {
        this.jdbc = jdbc;
        this.companies = companies;
    }

    @GetMapping
    public List<Map<String,Object>> list(@PathVariable UUID companyId) {
        companies.requireAccess(companyId);
        return jdbc.queryForList("""
            select id, service_offering_id as "serviceId", employee_id as "employeeId",
                   starts_at as "startsAt", ends_at as "endsAt", slots,
                   eligible_audience as "eligibleAudience", published_until as "publishedUntil",
                   notes, status, regular_price as "regularPrice", discount_type as "discountType",
                   discount_value as "discountValue", final_price as "finalPrice"
            from vacancy_opportunities
            where company_id = ?
            order by starts_at asc
            """, companyId);
    }

    @GetMapping("/public")
    public List<Map<String,Object>> publicList(@PathVariable UUID companyId) {
        return jdbc.queryForList("""
            select id, service_offering_id as "serviceId", employee_id as "employeeId",
                   starts_at as "startsAt", ends_at as "endsAt", slots,
                   eligible_audience as "eligibleAudience", published_until as "publishedUntil",
                   status, regular_price as "regularPrice", discount_type as "discountType",
                   discount_value as "discountValue", final_price as "finalPrice"
            from vacancy_opportunities
            where company_id = ?
              and status in ('PUBLISHED','ACTIVE')
              and (published_until is null or published_until > now())
            order by starts_at asc
            """, companyId);
    }

    @PostMapping
    @Transactional
    public Map<String,Object> create(@PathVariable UUID companyId, @RequestBody CreateOpportunityRequest request) {
        companies.requireAccess(companyId);
        final boolean discounted = request.discountType() != null;
        if (discounted && (request.discountValue() == null || request.discountValue().signum() < 0)) {
            throw new IllegalArgumentException("Desconto inválido");
        }
        BigDecimal finalPrice = request.regularPrice();
        if ("PERCENT".equalsIgnoreCase(request.discountType())) {
            if (request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) throw new IllegalArgumentException("Percentual acima de 100%");
            finalPrice = request.regularPrice().subtract(request.regularPrice().multiply(request.discountValue()).divide(BigDecimal.valueOf(100)));
        } else if ("FIXED".equalsIgnoreCase(request.discountType())) {
            finalPrice = request.regularPrice().subtract(request.discountValue());
        }
        if (finalPrice.signum() < 0) throw new IllegalArgumentException("Valor final não pode ser negativo");

        jdbc.update("""
            insert into vacancy_opportunities
                (id, created_at, updated_at, company_id, service_offering_id, employee_id,
                 starts_at, ends_at, slots, eligible_audience, published_until, notes,
                 status, regular_price, discount_type, discount_value, final_price)
            values (?, now(), now(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            UUID.randomUUID(), companyId, request.serviceId(), request.employeeId(),
            request.startsAt(), request.endsAt(), request.slots(), request.eligibleAudience(),
            request.publishedUntil(), request.notes(), request.status() == null ? "DRAFT" : request.status(),
            request.regularPrice(), request.discountType(), request.discountValue(), finalPrice);

        return Map.of("status", "created", "finalPrice", finalPrice);
    }

    @PostMapping("/{opportunityId}/reserve")
    @Transactional
    public Map<String,Object> reserve(@PathVariable UUID companyId, @PathVariable UUID opportunityId,
                                      @RequestParam UUID userId, @RequestParam(defaultValue = "5") int minutes) {
        // Reservation is a client-side flow; future auth context should derive userId from the JWT.
        if (minutes < 1 || minutes > 60) throw new IllegalArgumentException("Tempo de reserva inválido");
        jdbc.update("""
            update vacancy_reservations set status='EXPIRED', updated_at=now()
            where opportunity_id=? and status='HELD' and expires_at<=now()
            """, opportunityId);

        Integer held = jdbc.queryForObject("""
            select count(*) from vacancy_reservations
            where opportunity_id=? and status='HELD'
            """, Integer.class, opportunityId);
        Integer slots = jdbc.queryForObject("select slots from vacancy_opportunities where id=? and company_id=? and status in ('PUBLISHED','ACTIVE') and (published_until is null or published_until > now())", Integer.class, opportunityId, companyId);
        if (slots == null) throw new IllegalArgumentException("Oportunidade não encontrada ou expirada");
        if (held != null && held >= slots) throw new IllegalStateException("A vaga já está reservada");

        UUID id = UUID.randomUUID();
        jdbc.update("""
            insert into vacancy_reservations
              (id,created_at,updated_at,opportunity_id,user_id,expires_at,status)
            values (?,now(),now(),?,?,now() + (? * interval '1 minute'),'HELD')
            """, id, opportunityId, userId, minutes);
        return Map.of("reservationId", id, "expiresInMinutes", minutes);
    }

    public record CreateOpportunityRequest(UUID serviceId, UUID employeeId, java.time.Instant startsAt, java.time.Instant endsAt,
                                           int slots, String eligibleAudience, java.time.Instant publishedUntil, String notes,
                                           String status, BigDecimal regularPrice, String discountType, BigDecimal discountValue) {}
}
