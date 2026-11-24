package com.instrua.common.security;

import com.instrua.common.exception.BusinessException;
import com.instrua.users.CurrentUser;
import com.instrua.users.Role;
import com.instrua.users.User;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.UUID;

/** Central role boundary. Call from domain services; a role alone is not proof of company membership. */
@Service
public class DomainAccessService {
    private final CurrentUser currentUser;
    public DomainAccessService(CurrentUser currentUser) { this.currentUser = currentUser; }
    public User requireUser() { return currentUser.get(); }
    public void requireCompanyRole(UUID companyId, Set<Role> allowed) {
        User user = currentUser.get();
        if (user.getRoles().contains(Role.PLATFORM_ADMIN)) return;
        if (user.getRoles().stream().noneMatch(allowed::contains)) throw new BusinessException("Você não tem permissão para esta operação.");
        if (companyId == null) throw new BusinessException("Contexto de estabelecimento obrigatório.");
        // Caller must also verify an active persisted membership for this exact company/unit/team.
    }
    public void requireClinicalRole(UUID companyId) {
        User user = currentUser.get();
        if (!user.getRoles().contains(Role.PLATFORM_ADMIN) && !user.getRoles().contains(Role.CLINICAL))
            throw new BusinessException("Acesso restrito a profissionais clínicos autorizados.");
        if (companyId == null) throw new BusinessException("Contexto de atendimento obrigatório.");
    }
    public void requireBillingRole(UUID companyId) {
        User user = currentUser.get();
        if (!user.getRoles().contains(Role.PLATFORM_ADMIN) && !user.getRoles().contains(Role.BILLING)
                && !user.getRoles().contains(Role.COMPANY_OWNER) && !user.getRoles().contains(Role.COMPANY_ADMIN))
            throw new BusinessException("Acesso restrito ao financeiro/fiscal autorizado.");
        if (companyId == null) throw new BusinessException("Contexto de estabelecimento obrigatório.");
    }
}
