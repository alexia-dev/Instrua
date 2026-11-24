package com.instrua.clinical;

import com.instrua.companies.CompanyService;
import com.instrua.common.exception.BusinessException;
import com.instrua.common.exception.NotFoundException;
import com.instrua.common.security.DomainAccessService;
import com.instrua.users.Role;
import com.instrua.users.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/clinical/document-requests")
public class ClinicalDocumentRequestController {
    private final CompanyService companies;
    private final DomainAccessService access;
    private final ClinicalDocumentWorkflow workflow;
    private final JdbcTemplate jdbc;
    public ClinicalDocumentRequestController(CompanyService companies, DomainAccessService access, ClinicalDocumentWorkflow workflow, JdbcTemplate jdbc) {
        this.companies = companies; this.access = access; this.workflow = workflow; this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String, Object>> list(@PathVariable UUID companyId, @RequestParam(required = false) String status) {
        requireRequestAccess(companyId);
        if (status == null || status.isBlank()) return jdbc.queryForList("select id, patient_id, encounter_id, requested_by, document_type, purpose, status, assigned_clinician_id, decision_note, requested_at, decided_at from clinical_document_requests where company_id = ? order by requested_at desc limit 300", companyId);
        return jdbc.queryForList("select id, patient_id, encounter_id, requested_by, document_type, purpose, status, assigned_clinician_id, decision_note, requested_at, decided_at from clinical_document_requests where company_id = ? and status = ? order by requested_at desc limit 300", companyId, status.toUpperCase(Locale.ROOT));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@PathVariable UUID companyId, @Valid @RequestBody DocumentRequest request) {
        requireRequestAccess(companyId);
        Integer patientExists = jdbc.queryForObject("select count(*) from patients where id = ? and company_id = ? and active = true", Integer.class, request.patientId(), companyId);
        if (patientExists == null || patientExists == 0) throw new NotFoundException("Paciente não encontrado neste estabelecimento.");
        if (request.encounterId() != null) {
            Integer encounterExists = jdbc.queryForObject("select count(*) from care_encounters where id = ? and company_id = ? and patient_id = ?", Integer.class, request.encounterId(), companyId, request.patientId());
            if (encounterExists == null || encounterExists == 0) throw new NotFoundException("Atendimento não encontrado para este paciente.");
        }
        if (request.assignedClinicianId() != null) {
            Integer assigned = jdbc.queryForObject("select count(*) from user_role_assignments where user_id = ? and company_id = ? and role_code = 'CLINICAL' and revoked_at is null", Integer.class, request.assignedClinicianId(), companyId);
            if (assigned == null || assigned == 0) throw new BusinessException("O responsável precisa ter vínculo clínico ativo neste estabelecimento.");
        }
        User actor = access.requireUser(); UUID id = UUID.randomUUID();
        jdbc.update("insert into clinical_document_requests (id, company_id, patient_id, encounter_id, requested_by, document_type, purpose, status, assigned_clinician_id) values (?, ?, ?, ?, ?, ?, ?, 'REQUESTED', ?)",
                id, companyId, request.patientId(), request.encounterId(), actor.getId(), request.documentType().name(), request.purpose(), request.assignedClinicianId());
        return jdbc.queryForMap("select id, company_id, patient_id, encounter_id, requested_by, document_type, purpose, status, assigned_clinician_id, requested_at from clinical_document_requests where id = ?", id);
    }

    @PatchMapping("/{requestId}/status")
    public Map<String, Object> changeStatus(@PathVariable UUID companyId, @PathVariable UUID requestId, @Valid @RequestBody StatusChange request) {
        requireRequestAccess(companyId);
        Map<String, Object> existing;
        try { existing = jdbc.queryForMap("select status, assigned_clinician_id from clinical_document_requests where id = ? and company_id = ?", requestId, companyId); }
        catch (org.springframework.dao.EmptyResultDataAccessException ex) { throw new NotFoundException("Solicitação não encontrada."); }
        ClinicalDocumentWorkflow.Status current = ClinicalDocumentWorkflow.Status.valueOf(String.valueOf(existing.get("status")));
        ClinicalDocumentWorkflow.Status target = request.status();
        if (target == ClinicalDocumentWorkflow.Status.ISSUED || target == ClinicalDocumentWorkflow.Status.DELIVERED)
            throw new BusinessException("A emissão/entrega exige o serviço de documentos com armazenamento seguro e trilha de entrega, ainda não habilitado.");
        User actor = access.requireUser();
        UUID assignedId = existing.get("assigned_clinician_id") == null ? null : (existing.get("assigned_clinician_id") instanceof UUID u ? u : UUID.fromString(String.valueOf(existing.get("assigned_clinician_id"))));
        boolean isAssigned = assignedId != null && assignedId.equals(actor.getId());
        boolean canManage = actor.getRoles().contains(Role.COMPANY_OWNER) || actor.getRoles().contains(Role.COMPANY_ADMIN) || actor.getRoles().contains(Role.RECEPTION) || actor.getRoles().contains(Role.PLATFORM_ADMIN);
        workflow.transition(current, target, isAssigned, canManage);
        String decisionNote = request.decisionNote();
        Timestamp decidedAt = (target == ClinicalDocumentWorkflow.Status.REJECTED || target == ClinicalDocumentWorkflow.Status.APPROVED_FOR_ISSUE) ? Timestamp.from(Instant.now()) : null;
        jdbc.update("update clinical_document_requests set status = ?, decision_note = coalesce(?, decision_note), decided_at = coalesce(?, decided_at), updated_at = now() where id = ? and company_id = ?", target.name(), decisionNote, decidedAt, requestId, companyId);
        return jdbc.queryForMap("select id, patient_id, encounter_id, document_type, status, assigned_clinician_id, decision_note, requested_at, decided_at from clinical_document_requests where id = ? and company_id = ?", requestId, companyId);
    }

    private void requireRequestAccess(UUID companyId) {
        companies.requireAccess(companyId);
        access.requireCompanyRole(companyId, Set.of(Role.CLINICAL, Role.RECEPTION, Role.COMPANY_OWNER, Role.COMPANY_ADMIN));
    }
    public enum DocumentType { MEDICAL_CERTIFICATE, ATTENDANCE_DECLARATION, EXAM_REQUEST, REPORT, PRESCRIPTION_COPY, OTHER }
    public record DocumentRequest(@NotNull UUID patientId, UUID encounterId, @NotNull DocumentType documentType, String purpose, UUID assignedClinicianId) { }
    public record StatusChange(@NotNull ClinicalDocumentWorkflow.Status status, String decisionNote) { }
}
