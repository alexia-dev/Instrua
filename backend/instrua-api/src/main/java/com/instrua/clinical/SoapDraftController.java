package com.instrua.clinical;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instrua.common.audit.AuditService;
import com.instrua.common.exception.BusinessException;
import com.instrua.common.exception.NotFoundException;
import com.instrua.companies.CompanyService;
import com.instrua.users.CurrentUser;
import com.instrua.users.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * First real clinical editor: a clinician may save only their own DRAFT SOAP note
 * for an appointment assigned to their ACTIVE clinical employee membership.
 * No digital signature, prescription, AI diagnostic suggestion or cross-tenant lookup.
 * Neither reception, owners without clinical membership, nor platform admins can bypass
 * assignment checks merely by possessing an administrative role.
 */
@RestController
@RequestMapping("/api/v1/companies/{companyId}/clinical")
public class SoapDraftController {
    private final CompanyService companies;
    private final CurrentUser currentUser;
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final AuditService audit;

    public SoapDraftController(CompanyService companies, CurrentUser currentUser,
                               JdbcTemplate jdbc, ObjectMapper mapper, AuditService audit) {
        this.companies=companies; this.currentUser=currentUser; this.jdbc=jdbc;
        this.mapper=mapper; this.audit=audit;
    }

    @GetMapping("/appointments/{appointmentId}")
    @Transactional
    public Map<String,Object> appointment(@PathVariable UUID companyId, @PathVariable UUID appointmentId)
            throws JsonProcessingException {
        Map<String,Object> ap = requireOwnClinicalAppointment(companyId,appointmentId);
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("appointmentId",appointmentId);
        result.put("clientName",ap.get("client_name"));
        result.put("serviceName",ap.get("service_name"));
        result.put("professionalName",ap.get("professional_name"));
        result.put("startsAt",ap.get("starts_at"));
        result.put("appointmentStatus",ap.get("status"));
        UUID patientUser = (UUID)ap.get("patient_user_id");
        if(patientUser == null) patientUser=(UUID)ap.get("linked_user_id");
        List<Map<String,Object>> patients=patientUser==null?List.of():
            jdbc.queryForList("select id,name from patients where company_id=? and user_id=? and active=true order by created_at limit 1",companyId,patientUser);
        result.put("patientLinked",!patients.isEmpty());
        result.put("patientName",patients.isEmpty()?null:patients.get(0).get("name"));

        List<Map<String,Object>> encounters=jdbc.queryForList(
            "select id, patient_id, lead_professional_user_id, status, started_at from care_encounters where company_id=? and appointment_id=? order by started_at desc limit 1",companyId,appointmentId);
        if(encounters.isEmpty()){result.put("encounterId",null);result.put("soapDraft",null);}
        else{
            Map<String,Object> en=encounters.get(0);
            UUID uid=currentUser.get().getId();
            if(!uid.equals(en.get("lead_professional_user_id")))
                throw new BusinessException("Este prontuário pertence a outro profissional responsável.");
            if(!patients.isEmpty()&&!en.get("patient_id").equals(patients.get(0).get("id")))
                throw new BusinessException("O vínculo do paciente deste atendimento precisa ser revisado.");
            UUID encounterId=(UUID)en.get("id");
            result.put("encounterId",encounterId);
            result.put("encounterStatus",en.get("status"));
            result.put("soapDraft",ownDraft(encounterId,uid));
            audit.record(companyId,"CLINICAL_SOAP_VIEW","CareEncounter",encounterId,null);
        }
        return result;
    }

    @PostMapping("/appointments/{appointmentId}/encounter")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String,Object> start(@PathVariable UUID companyId, @PathVariable UUID appointmentId)
            throws JsonProcessingException {
        Map<String,Object> ap=requireOwnClinicalAppointment(companyId,appointmentId);
        if("CANCELLED".equals(ap.get("status"))||"NO_SHOW".equals(ap.get("status")))
            throw new BusinessException("Não é possível iniciar um prontuário de agendamento cancelado ou não realizado.");
        UUID userId=currentUser.get().getId();
        UUID patientUserId=(UUID)ap.get("patient_user_id");
        if(patientUserId==null)patientUserId=(UUID)ap.get("linked_user_id");
        if(patientUserId==null)throw new BusinessException(
            "A pessoa agendada ainda não tem uma identidade verificada e vinculada ao cadastro clínico.");
        List<UUID> patientIds=jdbc.query(
            "select id from patients where company_id=? and user_id=? and active=true order by created_at limit 1",
            (rs,n)->(UUID)rs.getObject("id"),companyId,patientUserId);
        if(patientIds.isEmpty())throw new BusinessException(
            "É necessário vincular o cadastro clínico deste paciente ao estabelecimento antes de registrar a evolução.");
        List<Map<String,Object>> existing=jdbc.queryForList(
            "select id, lead_professional_user_id from care_encounters where company_id=? and appointment_id=? limit 1",companyId,appointmentId);
        if(!existing.isEmpty()){
            UUID owner=(UUID)existing.get(0).get("lead_professional_user_id");
            if(!userId.equals(owner))throw new BusinessException("O atendimento clínico já está atribuído a outro profissional.");
            return appointment(companyId,appointmentId);
        }
        UUID encounterId=UUID.randomUUID();
        jdbc.update("insert into care_encounters(id,company_id,patient_id,appointment_id,lead_professional_user_id,status) values (?,?,?,?,?,'OPEN') on conflict (company_id,appointment_id) where appointment_id is not null do nothing",
            encounterId,companyId,patientIds.get(0),appointmentId,userId);
        audit.record(companyId,"CLINICAL_ENCOUNTER_STARTED","CareEncounter",encounterId,null);
        return appointment(companyId,appointmentId);
    }

    @PutMapping("/encounters/{encounterId}/soap-draft")
    @Transactional
    public DraftResponse saveDraft(@PathVariable UUID companyId,@PathVariable UUID encounterId,
                                   @Valid @RequestBody SoapRequest request) throws JsonProcessingException {
        UUID userId=requireOwnClinicalEncounter(companyId,encounterId);
        Map<String,String> note=new LinkedHashMap<>();
        note.put("subjective",safe(request.subjective()));
        note.put("objective",safe(request.objective()));
        note.put("assessment",safe(request.assessment()));
        note.put("plan",safe(request.plan()));
        String payload=mapper.writeValueAsString(note);
        UUID id=jdbc.queryForObject("""
            insert into clinical_records(id,encounter_id,record_type,content,author_user_id,status,source_system,authored_at,updated_at)
            values (?,?,'SOAP',?::jsonb,?,'DRAFT','INSTRUA',now(),now())
            on conflict (encounter_id,author_user_id,record_type)
                where status='DRAFT' and record_type='SOAP'
            do update set content=excluded.content,updated_at=now()
            returning id
            """,UUID.class,UUID.randomUUID(),encounterId,payload,userId);
        audit.record(companyId,"CLINICAL_SOAP_DRAFT_SAVED","ClinicalRecord",id,null);
        return ownDraft(encounterId,userId);
    }

    private UUID requireOwnClinicalEncounter(UUID companyId,UUID encounterId) {
        companies.requireAccess(companyId);
        UUID userId=currentUser.get().getId();
        Boolean allowed=jdbc.queryForObject("""
            select exists(
                select 1 from care_encounters en
                join appointments a on a.id=en.appointment_id and a.company_id=en.company_id
                join employees e on e.id=a.employee_id and e.company_id=en.company_id
                where en.id=? and en.company_id=? and en.lead_professional_user_id=?
                  and en.status='OPEN'
                  and e.account_user_id=? and e.access_role='CLINICAL' and e.active=true
            )
            """,Boolean.class,encounterId,companyId,userId,userId);
        if(!Boolean.TRUE.equals(allowed))throw new NotFoundException(
            "Atendimento clínico não encontrado ou indisponível para este profissional.");
        return userId;
    }

    private Map<String,Object> requireOwnClinicalAppointment(UUID companyId,UUID appointmentId) {
        companies.requireAccess(companyId);
        User actor=currentUser.get();
        List<Map<String,Object>> matches=jdbc.queryForList("""
            select a.id,a.patient_user_id,c.linked_user_id,c.name as client_name,
                   a.status,a.starts_at,svc.name as service_name,e.name as professional_name
              from appointments a
              join employees e on e.id=a.employee_id and e.company_id=a.company_id
              join clients c on c.id=a.client_id and c.company_id=a.company_id
              join service_offerings svc on svc.id=a.service_offering_id and svc.company_id=a.company_id
             where a.company_id=? and a.id=? and e.company_id=?
               and e.account_user_id=? and e.active=true and e.access_role='CLINICAL'
             limit 1
            """,companyId,appointmentId,companyId,actor.getId());
        if(matches.isEmpty())throw new NotFoundException(
            "Atendimento não encontrado ou não autorizado para este profissional.");
        return matches.get(0);
    }

    private DraftResponse ownDraft(UUID encounterId,UUID author) throws JsonProcessingException {
        List<Map<String,Object>> notes=jdbc.queryForList("""
            select id,content::text as content,updated_at
              from clinical_records where encounter_id=? and author_user_id=?
               and record_type='SOAP' and status='DRAFT'
             order by updated_at desc limit 1
            """,encounterId,author);
        if(notes.isEmpty())return null;
        Map<String,Object> row=notes.get(0);
        var content=mapper.readTree(String.valueOf(row.get("content")));
        return new DraftResponse((UUID)row.get("id"),encounterId,"DRAFT",
            content.path("subjective").asText(""),content.path("objective").asText(""),
            content.path("assessment").asText(""),content.path("plan").asText(""),
            row.get("updated_at"));
    }

    private static String safe(String text){return text==null?"":text.trim();}
    public record SoapRequest(@Size(max=10000) String subjective,@Size(max=10000) String objective,
                              @Size(max=10000) String assessment,@Size(max=10000) String plan){}
    public record DraftResponse(UUID id,UUID encounterId,String status,
                                String subjective,String objective,String assessment,String plan,Object updatedAt){}
}
