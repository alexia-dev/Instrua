package com.instrua.appointments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final DomainAccessService access;
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public CancellationPolicyController(CompanyService companies, DomainAccessService access, JdbcTemplate jdbc, ObjectMapper mapper) {
        this.companies = companies; this.access = access; this.jdbc = jdbc; this.mapper = mapper;
    }

    @GetMapping("/cancellation-policies")
    public List<Map<String, Object>> list(@PathVariable UUID companyId, @RequestParam(required = false) UUID serviceId) {
        companies.requireAccess(companyId);
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

    @PostMapping("/appointments/{appointmentId}/policy-acceptance")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> accept(@PathVariable UUID companyId, @PathVariable UUID appointmentId, @Valid @RequestBody AcceptanceRequest request) throws JsonProcessingException {
        companies.requireAccess(companyId);
        User user = access.requireUser();
        Map<String, Object> appointment;
        try { appointment = jdbc.queryForMap("select id, company_id, client_id, service_offering_id, status from appointments where id = ? and company_id = ?", appointmentId, companyId); }
        catch (org.springframework.dao.EmptyResultDataAccessException ex) { throw new NotFoundException("Agendamento não encontrado."); }
        if ("CANCELLED".equals(String.valueOf(appointment.get("status"))) || "COMPLETED".equals(String.valueOf(appointment.get("status")))) throw new BusinessException("Este agendamento já foi encerrado.");
        Map<String, Object> policy;
        try { policy = jdbc.queryForMap("select id, company_id, service_offering_id, version, title, policy_text, deposit_amount, cancellation_deadline_minutes, late_cancellation_fee, no_show_fee, currency, effective_from, effective_until from cancellation_policies where id = ? and company_id = ? and effective_from <= now() and (effective_until is null or effective_until > now())", request.policyId(), companyId); }
        catch (org.springframework.dao.EmptyResultDataAccessException ex) { throw new NotFoundException("Política ativa não encontrada."); }
        Object policyService = policy.get("service_offering_id");
        if (policyService != null && !policyService.equals(appointment.get("service_offering_id"))) throw new BusinessException("Esta política não se aplica ao serviço agendado.");
        Integer exists = jdbc.queryForObject("select count(*) from appointment_policy_acceptances where appointment_id = ?", Integer.class, appointmentId);
        if (exists != null && exists > 0) throw new BusinessException("Já existe uma política aceita para este agendamento. Não sobrescrevemos o aceite original.");
        Instant acceptedAt = Instant.now();
        Map<String, Object> snapshot = new LinkedHashMap<>(policy);
        snapshot.put("acceptedAt", acceptedAt.toString());
        snapshot.put("acceptedByUserId", user.getId().toString());
        snapshot.put("acceptanceChannel", request.acceptanceChannel());
        UUID acceptanceId = UUID.randomUUID();
        jdbc.update("insert into appointment_policy_acceptances (id, appointment_id, policy_id, accepted_by_user_id, accepted_at, policy_snapshot, acceptance_channel) values (?, ?, ?, ?, ?, ?::jsonb, ?)",
                acceptanceId, appointmentId, request.policyId(), user.getId(), Timestamp.from(acceptedAt), mapper.writeValueAsString(snapshot), request.acceptanceChannel());
        return jdbc.queryForMap("select id, appointment_id, policy_id, accepted_by_user_id, accepted_at, policy_snapshot, acceptance_channel from appointment_policy_acceptances where id = ?", acceptanceId);
    }

    public record PolicyRequest(@NotNull UUID serviceOfferingId, @NotBlank String title, @NotBlank String policyText,
            @DecimalMin("0.00") BigDecimal depositAmount, @Min(0) Integer cancellationDeadlineMinutes,
            @DecimalMin("0.00") BigDecimal lateCancellationFee, @DecimalMin("0.00") BigDecimal noShowFee,
            @NotBlank String currency, @NotNull Instant effectiveFrom, Instant effectiveUntil) {
        public PolicyRequest { if (currency == null || currency.isBlank()) currency = "BRL"; }
    }
    public record AcceptanceRequest(@NotNull UUID policyId, @NotBlank String acceptanceChannel) { }
}
