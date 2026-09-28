package com.instrua.companies;

import com.instrua.common.exception.BusinessException;
import com.instrua.common.tenant.TenantAccessService;
import com.instrua.users.CurrentUser;
import com.instrua.users.User;
import com.instrua.users.Role;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {
    private final CompanyRepository companies;
    private final CurrentUser currentUser;
    private final TenantAccessService tenantAccess;

    public CompanyService(CompanyRepository companies, CurrentUser currentUser, TenantAccessService tenantAccess) {
        this.companies = companies;
        this.currentUser = currentUser;
        this.tenantAccess = tenantAccess;
    }

    @Transactional
    public Company create(String name, String slug, String email, String phone, String timezone, User owner) {
        String normalizedSlug = slug.trim().toLowerCase();
        if (!normalizedSlug.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            throw new BusinessException("O identificador da empresa deve usar letras minúsculas, números e hífens");
        }
        if (companies.existsBySlugIgnoreCase(normalizedSlug)) throw new BusinessException("Este identificador de empresa já está em uso");
        return companies.save(new Company(name.trim(), normalizedSlug, email, phone, timezone, owner));
    }

    @Transactional(readOnly = true)
    public List<Company> accessible() {
        User user = currentUser.get();
        if (user.getRoles().contains(Role.PLATFORM_ADMIN)) return companies.findAll();
        Map<UUID, Company> unique = new LinkedHashMap<>();
        companies.findAllByOwnerId(user.getId()).forEach(company -> unique.put(company.getId(), company));
        companies.findAllAccessibleByUserId(user.getId()).forEach(company -> unique.put(company.getId(), company));
        return new ArrayList<>(unique.values());
    }

    @Transactional(readOnly = true)
    public Company requireAccess(UUID companyId) {
        return tenantAccess.requireAccess(companyId);
    }
}
