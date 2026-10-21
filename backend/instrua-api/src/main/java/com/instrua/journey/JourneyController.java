package com.instrua.journey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instrua.companies.CompanyService;
import com.instrua.common.exception.BusinessException;
import com.instrua.common.exception.NotFoundException;
import com.instrua.common.security.DomainAccessService;
import com.instrua.users.Role;
import com.instrua.users.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/journey")
public class JourneyController {
    private final CompanyService companies; private final DomainAccessService access; private final JourneyCorrectionPolicy corrections; private final JdbcTemplate jdbc; private final ObjectMapper mapper;
    public JourneyController(CompanyService companies, DomainAccessService access, JourneyCorrectionPolicy corrections, JdbcTemplate jdbc, ObjectMapper mapper) { this.companies=companies; this.access=access; this.corrections=corrections; this.jdbc=jdbc; this.mapper=mapper; }

    @PostMapping("/me/events")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> clockEvent(@PathVariable UUID companyId, @Valid @RequestBody JourneyEventRequest request) {
        companies.requireAccess(companyId); User user=access.requireUser();
        UUID employeeId;
        try { employeeId=jdbc.queryForObject("select id from employees where company_id = ? and account_user_id = ? and active = true", UUID.class, companyId, user.getId()); }
        catch (org.springframework.dao.EmptyResultDataAccessException ex) { throw new BusinessException("Sua conta não está vinculada como colaborador ativo deste estabelecimento."); }
        UUID id=UUID.randomUUID(); Instant occurred=Instant.now();
        jdbc.update("insert into journey_events (id, company_id, user_id, employee_id, event_type, occurred_at, source, location_reference) values (?, ?, ?, ?, ?, ?, 'SELF_SERVICE', ?)", id, companyId, user.getId(), employeeId, request.eventType().name(), Timestamp.from(occurred), request.locationReference());
        return jdbc.queryForMap("select id, company_id, user_id, employee_id, event_type, occurred_at, source, location_reference from journey_events where id = ?", id);
    }

    @GetMapping("/me/events")
    public List<Map<String,Object>> myEvents(@PathVariable UUID companyId) {
        companies.requireAccess(companyId); User user=access.requireUser();
        return jdbc.queryForList("select id, event_type, occurred_at, source, location_reference from journey_events where company_id = ? and user_id = ? order by occurred_at desc limit 300", companyId, user.getId());
    }

    @PostMapping("/events/{eventId}/corrections")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> requestCorrection(@PathVariable UUID companyId, @PathVariable UUID eventId, @Valid @RequestBody CorrectionRequest request) throws JsonProcessingException {
        companies.requireAccess(companyId); User user=access.requireUser();
        Integer owns=jdbc.queryForObject("select count(*) from journey_events where id = ? and company_id = ? and user_id = ?", Integer.class, eventId, companyId, user.getId());
        if (owns == null || owns == 0) throw new NotFoundException("Registro não encontrado.");
        UUID id=UUID.randomUUID();
        jdbc.update("insert into journey_correction_requests (id, company_id, requested_by, journey_event_id, requested_change, justification, status) values (?, ?, ?, ?, ?::jsonb, ?, 'PENDING')", id, companyId, user.getId(), eventId, mapper.writeValueAsString(request.requestedChange()), request.justification().trim());
        return jdbc.queryForMap("select id, company_id, requested_by, journey_event_id, requested_change, justification, status, created_at from journey_correction_requests where id = ?", id);
    }

    @GetMapping("/corrections")
    public List<Map<String,Object>> corrections(@PathVariable UUID companyId, @RequestParam(required=false) String status) {
        requireReviewer(companyId);
        if(status==null || status.isBlank()) return jdbc.queryForList("select id, requested_by, journey_event_id, requested_change, justification, status, decided_by, decided_at, created_at from journey_correction_requests where company_id = ? order by created_at desc limit 300", companyId);
        return jdbc.queryForList("select id, requested_by, journey_event_id, requested_change, justification, status, decided_by, decided_at, created_at from journey_correction_requests where company_id = ? and status = ? order by created_at desc limit 300", companyId, status.toUpperCase(Locale.ROOT));
    }

    @PatchMapping("/corrections/{requestId}")
    public Map<String,Object> decideCorrection(@PathVariable UUID companyId, @PathVariable UUID requestId, @Valid @RequestBody CorrectionDecision request) {
        requireReviewer(companyId); User reviewer=access.requireUser(); Map<String,Object> row;
        try { row=jdbc.queryForMap("select status, requested_by from journey_correction_requests where id = ? and company_id = ?", requestId, companyId); }
        catch(org.springframework.dao.EmptyResultDataAccessException ex) { throw new NotFoundException("Solicitação de correção não encontrada."); }
        JourneyCorrectionPolicy.Status current=JourneyCorrectionPolicy.Status.valueOf(String.valueOf(row.get("status")));
        boolean requester=reviewer.getId().equals(row.get("requested_by") instanceof UUID u ? u : UUID.fromString(String.valueOf(row.get("requested_by"))));
        corrections.decide(current, JourneyCorrectionPolicy.Status.valueOf(request.status().name()), true, requester);
        jdbc.update("update journey_correction_requests set status = ?, decided_by = ?, decided_at = now(), updated_at = now() where id = ? and company_id = ? and status = 'PENDING'", request.status().name(), reviewer.getId(), requestId, companyId);
        return jdbc.queryForMap("select id, requested_by, journey_event_id, requested_change, justification, status, decided_by, decided_at, created_at from journey_correction_requests where id = ?", requestId);
    }

    private void requireReviewer(UUID companyId) {
        companies.requireAccess(companyId);
        access.requireCompanyRole(companyId, Set.of(Role.COMPANY_OWNER, Role.COMPANY_ADMIN, Role.PLATFORM_ADMIN));
    }
    public enum EventType { ENTRY, EXIT, BREAK_START, BREAK_END }
    public enum Decision { APPROVED, REJECTED }
    public record JourneyEventRequest(@NotNull EventType eventType, String locationReference) { }
    public record CorrectionRequest(@NotNull Map<String,Object> requestedChange, @NotBlank String justification) { }
    public record CorrectionDecision(@NotNull Decision status) { }
}
