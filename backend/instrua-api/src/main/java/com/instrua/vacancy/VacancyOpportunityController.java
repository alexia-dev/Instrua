package com.instrua.vacancy;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/vacancy-opportunities")
public class VacancyOpportunityController {
    private final JdbcTemplate jdbc;

    public VacancyOpportunityController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String,Object>> list(@PathVariable UUID companyId) {
        return jdbc.queryForList("""
            select id, service_offering_id as "serviceId", employee_id as "employeeId",
                   starts_at as "startsAt", ends_at as "endsAt", slots,
                   eligible_audience as "eligibleAudience", published_until as "publishedUntil",
                   status, regular_price as "regularPrice", discount_type as "discountType",
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
}