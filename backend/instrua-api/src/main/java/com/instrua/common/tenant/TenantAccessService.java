package com.instrua.common.tenant;

import com.instrua.companies.Company;
import com.instrua.companies.CompanyRepository;
import com.instrua.common.exception.NotFoundException;
import com.instrua.users.CurrentUser;
import com.instrua.users.Role;
import com.instrua.users.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TenantAccessService {
    private final CompanyRepository companies;
    private final CurrentUser currentUser;

    public TenantAccessService(CompanyRepository companies, CurrentUser currentUser) {
        this.companies = companies;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public Company requireAccess(UUID companyId) {
        User user = currentUser.get();
        if (user.getRoles().contains(Role.PLATFORM_ADMIN)) {
            return companies.findById(companyId)
                    .orElseThrow(() -> new NotFoundException("Empresa não encontrada"));
        }
        return companies.findAccessibleForUser(companyId, user.getId())
                .orElseThrow(() -> new NotFoundException("Empresa não encontrada"));
    }
}
