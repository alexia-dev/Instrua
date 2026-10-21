package com.instrua.opportunities;

import com.instrua.companies.CompanyService;
import com.instrua.common.exception.BusinessException;
import com.instrua.common.exception.NotFoundException;
import com.instrua.common.security.DomainAccessService;
import com.instrua.users.Role;
import com.instrua.users.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class OpportunityController {
    private final CompanyService companies; private final DomainAccessService access; private final JdbcTemplate jdbc;
    public OpportunityController(CompanyService companies, DomainAccessService access, JdbcTemplate jdbc) { this.companies=companies; this.access=access; this.jdbc=jdbc; }

    @GetMapping("/opportunities")
    public List<Map<String,Object>> catalogue(@RequestParam(required=false) String type) {
        if(type==null || type.isBlank()) return jdbc.queryForList("select o.id, o.company_id, c.name as company_name, o.title, o.description, o.opportunity_type, o.status, o.price, o.currency, o.starts_at, o.ends_at from product_opportunities o join companies c on c.id = o.company_id where o.status = 'OPEN' and c.active = true and (o.starts_at is null or o.starts_at <= now()) and (o.ends_at is null or o.ends_at > now()) order by o.created_at desc limit 200");
        return jdbc.queryForList("select o.id, o.company_id, c.name as company_name, o.title, o.description, o.opportunity_type, o.status, o.price, o.currency, o.starts_at, o.ends_at from product_opportunities o join companies c on c.id = o.company_id where o.status = 'OPEN' and o.opportunity_type = ? and c.active = true and (o.starts_at is null or o.starts_at <= now()) and (o.ends_at is null or o.ends_at > now()) order by o.created_at desc limit 200",type.toUpperCase(Locale.ROOT));
    }

    @PostMapping("/companies/{companyId}/opportunities")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> create(@PathVariable UUID companyId, @Valid @RequestBody OpportunityRequest request) {
        companies.requireAccess(companyId); access.requireCompanyRole(companyId,Set.of(Role.COMPANY_OWNER,Role.COMPANY_ADMIN));
        if(request.endsAt()!=null && request.startsAt()!=null && !request.endsAt().isAfter(request.startsAt())) throw new BusinessException("A data final precisa ser posterior à inicial.");
        if(request.price()!=null && request.price().signum()<0) throw new BusinessException("O valor não pode ser negativo.");
        User actor=access.requireUser(); UUID id=UUID.randomUUID();
        jdbc.update("insert into product_opportunities (id, company_id, title, description, opportunity_type, status, price, currency, starts_at, ends_at, published_by) values (?, ?, ?, ?, ?, 'OPEN', ?, ?, ?, ?, ?)",id,companyId,request.title().trim(),request.description(),request.type().name(),request.price(),request.currency().toUpperCase(Locale.ROOT),request.startsAt()==null?null:Timestamp.from(request.startsAt()),request.endsAt()==null?null:Timestamp.from(request.endsAt()),actor.getId());
        return jdbc.queryForMap("select id, company_id, title, description, opportunity_type, status, price, currency, starts_at, ends_at, published_by, created_at from product_opportunities where id = ?",id);
    }

    @PostMapping("/opportunities/{opportunityId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> apply(@PathVariable UUID opportunityId, @Valid @RequestBody ApplicationRequest request) {
        User actor=access.requireUser();
        Integer open=jdbc.queryForObject("select count(*) from product_opportunities where id = ? and status = 'OPEN' and (starts_at is null or starts_at <= now()) and (ends_at is null or ends_at > now())",Integer.class,opportunityId);
        if(open==null || open==0) throw new NotFoundException("Oportunidade indisponível.");
        UUID id=UUID.randomUUID();
        try { jdbc.update("insert into opportunity_applications (id, opportunity_id, applicant_user_id, message, status) values (?, ?, ?, ?, 'SUBMITTED')",id,opportunityId,actor.getId(),request.message()); }
        catch(org.springframework.dao.DuplicateKeyException ex) { throw new BusinessException("Você já enviou candidatura/reserva para esta oportunidade."); }
        return jdbc.queryForMap("select id, opportunity_id, applicant_user_id, message, status, created_at from opportunity_applications where id = ?",id);
    }

    public enum Type { SERVICE, VACANCY, EVENT, SLOT, PRODUCT }
    public record OpportunityRequest(@NotBlank @Size(max=255) String title, @Size(max=4000) String description, @NotNull Type type, @DecimalMin("0.00") BigDecimal price, @NotBlank String currency, Instant startsAt, Instant endsAt) { public OpportunityRequest { if(currency==null || currency.isBlank()) currency="BRL"; } }
    public record ApplicationRequest(@Size(max=2000) String message) { }
}
