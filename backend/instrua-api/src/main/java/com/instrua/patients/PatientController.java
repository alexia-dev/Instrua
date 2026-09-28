package com.instrua.patients;

import com.instrua.companies.Company;
import com.instrua.companies.CompanyRepository;
import com.instrua.common.audit.AuditService;
import com.instrua.common.tenant.TenantAccessService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/patients")
public class PatientController {
    private final TenantAccessService tenants;
    private final PatientRepository patients;
    private final AuditService audit;

    public PatientController(TenantAccessService tenants, PatientRepository patients, AuditService audit) {
        this.tenants = tenants;
        this.patients = patients;
        this.audit = audit;
    }

    @GetMapping
    public List<PatientResponse> list(@PathVariable UUID companyId) {
        tenants.requireAccess(companyId);
        return patients.findAllByCompanyIdOrderByNameAsc(companyId).stream().map(PatientResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse create(@PathVariable UUID companyId, @Valid @RequestBody PatientRequest request) {
        Company company = tenants.requireAccess(companyId);
        Patient saved = patients.save(new Patient(company, request.name(), request.email(), request.phone(), request.documentNumber(), request.notes(), request.userId()));
        audit.record(companyId, "PATIENT_CREATED", "Patient", saved.getId(), null);
        return PatientResponse.from(saved);
    }

    public record PatientRequest(@NotBlank String name, @Email String email, String phone, String documentNumber, String notes, UUID userId) { }

    public record PatientResponse(UUID id, String name, String email, String phone, String documentNumber, String notes, boolean active, UUID userId) {
        static PatientResponse from(Patient patient) {
            return new PatientResponse(patient.getId(), patient.getName(), patient.getEmail(), patient.getPhone(), patient.getDocumentNumber(), patient.getNotes(), patient.isActive(), patient.getUserId());
        }
    }
}
