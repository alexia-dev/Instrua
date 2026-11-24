package com.instrua.companies;

import com.instrua.common.exception.BusinessException;
import com.instrua.users.CurrentUser;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/dashboard")
public class BusinessDashboardController {
    private final CompanyService companies;
    private final CurrentUser currentUser;
    private final JdbcTemplate jdbc;
    public BusinessDashboardController(CompanyService companies,CurrentUser currentUser,JdbcTemplate jdbc) {
        this.companies=companies;this.currentUser=currentUser;this.jdbc=jdbc;
    }

    @GetMapping("/sales")
    @Transactional(readOnly=true)
    public Map<String,Object> sales(@PathVariable UUID companyId) {
        Company company=companies.requireAccess(companyId);
        UUID userId=currentUser.get().getId();
        Boolean member=jdbc.queryForObject("""
            select exists(select 1 from employees where company_id=? and account_user_id=? and active=true
                and access_role in ('COMPANY_OWNER','COMPANY_ADMIN','BILLING'))
            """,Boolean.class,companyId,userId);
        if(!company.getOwner().getId().equals(userId)&&!Boolean.TRUE.equals(member)) {
            throw new BusinessException("Dados de vendas restritos à administração e ao financeiro.");
        }
        ZoneId tz=ZoneId.of(company.getTimezone());
        LocalDate day=LocalDate.now(tz);
        Instant start=day.atStartOfDay(tz).toInstant();
        Instant end=day.plusDays(1).atStartOfDay(tz).toInstant();
        Map<String,Object> row=jdbc.queryForMap("""
            select count(*) as orders_today,coalesce(sum(total),0) as sales_today
            from business_orders where company_id=? and status in ('PAID','COMPLETED','SETTLED')
                and created_at>=? and created_at<?
            """,companyId,Timestamp.from(start),Timestamp.from(end));
        return Map.of("date",day.toString(),"ordersToday",row.get("orders_today"),
            "salesToday",row.getOrDefault("sales_today",BigDecimal.ZERO),"currency","BRL",
            "note","Somente pedidos registrados com status de pagamento concluído; não inclui vendas externas.");
    }
}
