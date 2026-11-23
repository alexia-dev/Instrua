package com.instrua.common.security;

import com.instrua.companies.Company;
import com.instrua.companies.CompanyService;
import com.instrua.common.exception.BusinessException;
import com.instrua.users.CurrentUser;
import com.instrua.users.User;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Authorizes a mutation using persisted company membership, never a role
 * selected in the browser or a global role alone.
 */
@Service
public class CompanyPermissionService {
    private final CompanyService companies;
    private final CurrentUser currentUser;
    private final JdbcTemplate jdbc;

    public CompanyPermissionService(CompanyService companies, CurrentUser currentUser, JdbcTemplate jdbc) {
        this.companies = companies;
        this.currentUser = currentUser;
        this.jdbc = jdbc;
    }

    public Company requireManager(UUID companyId) {
        return require(companyId, false);
    }

    public Company requireScheduling(UUID companyId) {
        return require(companyId, true);
    }

    private Company require(UUID companyId, boolean scheduling) {
        Company company = companies.requireAccess(companyId);
        User user = currentUser.get();
        if (company.getOwner().getId().equals(user.getId())) return company;
        String roles = scheduling
                ? "'COMPANY_OWNER','COMPANY_ADMIN','RECEPTION','CLINICAL'"
                : "'COMPANY_OWNER','COMPANY_ADMIN'";
        Boolean permitted = jdbc.queryForObject(
                "select exists(select 1 from employees where company_id = ? and account_user_id = ? and active = true and access_role in (" + roles + "))",
                Boolean.class, companyId, user.getId());
        if (!Boolean.TRUE.equals(permitted)) {
            throw new BusinessException("Esta conta não tem permissão para modificar dados do estabelecimento.");
        }
        return company;
    }
}
