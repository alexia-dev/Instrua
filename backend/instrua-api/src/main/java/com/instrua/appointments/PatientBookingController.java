package com.instrua.appointments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instrua.clients.Client;
import com.instrua.clients.ClientRepository;
import com.instrua.companies.Company;
import com.instrua.companies.CompanyRepository;
import com.instrua.common.exception.BusinessException;
import com.instrua.common.exception.NotFoundException;
import com.instrua.employees.Employee;
import com.instrua.employees.EmployeeRepository;
import com.instrua.patients.Patient;
import com.instrua.patients.PatientRepository;
import com.instrua.services.ServiceOffering;
import com.instrua.services.ServiceOfferingRepository;
import com.instrua.users.CurrentUser;
import com.instrua.users.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class PatientBookingController {
    private final CurrentUser currentUser;
    private final CompanyRepository companies;
    private final ClientRepository clients;
    private final PatientRepository patients;
    private final EmployeeRepository employees;
    private final ServiceOfferingRepository services;
    private final AppointmentRepository appointments;
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public PatientBookingController(CurrentUser currentUser, CompanyRepository companies, ClientRepository clients, PatientRepository patients,
            EmployeeRepository employees, ServiceOfferingRepository services, AppointmentRepository appointments, JdbcTemplate jdbc, ObjectMapper mapper) {
        this.currentUser=currentUser; this.companies=companies; this.clients=clients; this.patients=patients; this.employees=employees;
        this.services=services; this.appointments=appointments; this.jdbc=jdbc; this.mapper=mapper;
    }

    @GetMapping("/me/appointments")
    @Transactional(readOnly=true)
    public List<AppointmentController.AppointmentResponse> myAppointments() {
        User user=currentUser.get();
        return appointments.findAllByPatientUserIdOrderByStartsAtAsc(user.getId()).stream().map(AppointmentController.AppointmentResponse::from).toList();
    }

    @PostMapping("/companies/{companyId}/appointments/self")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public BookingResponse book(@PathVariable UUID companyId, @Valid @RequestBody BookingRequest request) throws JsonProcessingException {
        User user=currentUser.get();
        Company company=companies.findById(companyId).orElseThrow(() -> new NotFoundException("Estabelecimento não encontrado."));
        if (!company.isActive()) throw new NotFoundException("Estabelecimento indisponível.");
        ServiceOffering offering=services.findByIdAndCompanyIdAndActiveTrue(request.serviceId(),companyId).orElseThrow(() -> new NotFoundException("Serviço não encontrado neste estabelecimento."));
        Employee employee=request.employeeId()==null ? null : employees.findByIdAndCompanyIdAndActiveTrue(request.employeeId(),companyId).orElseThrow(() -> new NotFoundException("Profissional não encontrado neste estabelecimento."));
        if (request.startsAt().isBefore(Instant.now().plus(1, ChronoUnit.MINUTES))) throw new BusinessException("Escolha um horário futuro.");
        Instant endsAt=request.startsAt().plus(offering.getDurationMinutes(), ChronoUnit.MINUTES);
        if(employee!=null && appointments.existsByCompanyIdAndEmployeeIdAndStartsAtLessThanAndEndsAtGreaterThanAndStatusIn(companyId,employee.getId(),endsAt,request.startsAt(),EnumSet.of(AppointmentStatus.SCHEDULED,AppointmentStatus.CONFIRMED)))
            throw new BusinessException("Esse horário acabou de ficar indisponível. Escolha outro horário.");

        Map<String,Object> profile=profileFor(user.getId());
        String phone=(String)profile.get("phone");
        String documentNumber=(String)profile.get("document_number");
        Patient patient=patients.findFirstByCompanyIdAndUserIdAndActiveTrue(companyId,user.getId()).orElseGet(() -> patients.save(new Patient(company,user.getName(),user.getEmail(),phone,documentNumber,null,user.getId())));
        Client client=clients.findFirstByCompanyIdAndEmailIgnoreCaseAndNameAndActiveTrue(companyId,user.getEmail(),user.getName())
            .orElseGet(() -> clients.save(new Client(company,user.getName(),user.getEmail(),phone,documentNumber,null)));

        Map<String,Object> policy=activePolicy(companyId,offering.getId());
        if(policy!=null) {
            UUID requiredPolicyId=asUuid(policy.get("id"));
            if(!Boolean.TRUE.equals(request.policyAccepted()) || request.cancellationPolicyId()==null || !request.cancellationPolicyId().equals(requiredPolicyId))
                throw new BusinessException("Antes de reservar, leia e aceite a política de cancelamento vigente. Atualize a política exibida e tente novamente.");
        } else if(request.policyAccepted()!=null && request.policyAccepted() && request.cancellationPolicyId()!=null) {
            throw new BusinessException("A política indicada não está vigente para esse serviço.");
        }

        Appointment saved=appointments.save(new Appointment(company,client,employee,offering,user,request.startsAt(),endsAt,request.notes()));
        if(policy!=null) {
            Instant acceptedAt=Instant.now();
            Map<String,Object> snapshot=new LinkedHashMap<>(policy); snapshot.put("accepted_at",acceptedAt.toString()); snapshot.put("accepted_by_user_id",user.getId().toString()); snapshot.put("acceptance_channel","PATIENT_APP");
            jdbc.update("insert into appointment_policy_acceptances (id, appointment_id, policy_id, accepted_by_user_id, accepted_at, policy_snapshot, acceptance_channel) values (?, ?, ?, ?, ?, ?::jsonb, 'PATIENT_APP')",
                    UUID.randomUUID(),saved.getId(),requiredPolicyId(policy),user.getId(),Timestamp.from(acceptedAt),mapper.writeValueAsString(snapshot));
            jdbc.update("update appointments set cancellation_policy_id = ? where id = ?",requiredPolicyId(policy),saved.getId());
        }
        return new BookingResponse(AppointmentController.AppointmentResponse.from(saved),policy!=null,policy==null?null:String.valueOf(policy.get("policy_text")),policy==null?null:(Integer)policy.get("cancellation_deadline_minutes"));
    }

    @PostMapping("/me/appointments/{appointmentId}/cancel")
    @Transactional
    public CancellationResponse cancel(@PathVariable UUID appointmentId, @Valid @RequestBody CancellationRequest request) throws JsonProcessingException {
        User user=currentUser.get();
        Appointment appointment=appointments.findByIdAndPatientUserId(appointmentId,user.getId()).orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));
        if(appointment.getStatus()==AppointmentStatus.CANCELLED || appointment.getStatus()==AppointmentStatus.COMPLETED || appointment.getStatus()==AppointmentStatus.NO_SHOW)
            throw new BusinessException("Este agendamento já foi encerrado.");
        BigDecimal fee=BigDecimal.ZERO; boolean late=false;
        try {
            Map<String,Object> acceptance=jdbc.queryForMap("select policy_snapshot from appointment_policy_acceptances where appointment_id = ?",appointmentId);
            JsonNode snap=mapper.readTree(String.valueOf(acceptance.get("policy_snapshot")));
            int deadline=snap.path("cancellation_deadline_minutes").asInt(0);
            Instant now=Instant.now();
            late=deadline>0 && Duration.between(now,appointment.getStartsAt()).toMinutes()<deadline;
            if(late && snap.path("late_cancellation_fee").isNumber()) fee=snap.path("late_cancellation_fee").decimalValue();
        } catch(EmptyResultDataAccessException ignored) { /* No accepted cancellation policy: no fee is assessed here. */ }
        if(fee.signum()>0 && !Boolean.TRUE.equals(request.confirmPotentialFee()))
            throw new BusinessException("A política aceita prevê taxa de cancelamento tardio de R$ "+fee.toPlainString()+". Confirme essa condição para concluir o cancelamento.");
        appointment.changeStatus(AppointmentStatus.CANCELLED,request.reason().trim());
        jdbc.update("update appointments set cancellation_initiator = 'PATIENT' where id = ?",appointmentId);
        return new CancellationResponse(appointmentId,AppointmentStatus.CANCELLED,fee,late,"NOT_CHARGED", "Taxa calculada conforme a política aceita; nenhum valor foi cobrado por esta API.");
    }

    private Map<String,Object> profileFor(UUID userId) {
        try { return jdbc.queryForMap("select phone, document_number from user_profiles where user_id = ?",userId); }
        catch(EmptyResultDataAccessException ex) { return Map.of(); }
    }
    private Map<String,Object> activePolicy(UUID companyId, UUID serviceId) {
        try { return jdbc.queryForMap("select id, service_offering_id, version, title, policy_text, deposit_amount, cancellation_deadline_minutes, late_cancellation_fee, no_show_fee, currency, effective_from, effective_until from cancellation_policies where company_id = ? and (service_offering_id = ? or service_offering_id is null) and effective_from <= now() and (effective_until is null or effective_until > now()) order by case when service_offering_id = ? then 0 else 1 end, version desc limit 1",companyId,serviceId,serviceId); }
        catch(EmptyResultDataAccessException ex) { return null; }
    }
    private UUID requiredPolicyId(Map<String,Object> policy) { return asUuid(policy.get("id")); }
    private UUID asUuid(Object value) { return value instanceof UUID u ? u : UUID.fromString(String.valueOf(value)); }

    public record BookingRequest(@NotNull UUID serviceId, UUID employeeId, @NotNull @Future Instant startsAt, @Size(max=2000) String notes, UUID cancellationPolicyId, Boolean policyAccepted) { }
    public record BookingResponse(AppointmentController.AppointmentResponse appointment, boolean cancellationPolicyAccepted, String policyText, Integer cancellationDeadlineMinutes) { }
    public record CancellationRequest(@jakarta.validation.constraints.NotBlank @Size(max=1000) String reason, Boolean confirmPotentialFee) { }
    public record CancellationResponse(UUID appointmentId, AppointmentStatus status, BigDecimal feeAssessment, boolean lateCancellation, String paymentStatus, String notice) { }
}
