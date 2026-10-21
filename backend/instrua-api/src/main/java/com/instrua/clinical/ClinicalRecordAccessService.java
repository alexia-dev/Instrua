package com.instrua.clinical;

import com.instrua.common.exception.BusinessException;
import com.instrua.common.security.DomainAccessService;
import com.instrua.users.Role;
import com.instrua.users.User;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * This decision point must be called after loading and verifying the patient, encounter and
 * organization relationship. It is not a substitute for tenant-filtered repository queries.
 */
@Service
public class ClinicalRecordAccessService {
    private final DomainAccessService access;
    public ClinicalRecordAccessService(DomainAccessService access) { this.access = access; }
    public void requireCareContext(UUID companyId, UUID patientId, UUID encounterId,
                                   boolean hasActiveGrant, boolean isLinkedCareTeamMember,
                                   boolean isPatientSelfAccess) {
        User user = access.requireUser();
        if (isPatientSelfAccess) return; // Caller must verify patientId belongs to this account.
        if (!user.getRoles().contains(Role.PLATFORM_ADMIN) && !user.getRoles().contains(Role.CLINICAL))
            throw new BusinessException("Prontuário restrito ao paciente e à equipe clínica autorizada.");
        if (!(hasActiveGrant || isLinkedCareTeamMember))
            throw new BusinessException("Não existe vínculo assistencial ativo para este acesso.");
        if (companyId == null || patientId == null || encounterId == null)
            throw new BusinessException("Contexto assistencial incompleto.");
    }
}
