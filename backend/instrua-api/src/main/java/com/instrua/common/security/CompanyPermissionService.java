package com.instrua.common.security;

import com.instrua.companies.Company;
import com.instrua.companies.CompanyService;
import com.instrua.common.exception.BusinessException;
import com.instrua.users.CurrentUser;
import com.instrua.users.User;
import java.util.List;
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

    /**
     * Only reception and managers can see the complete establishment schedule.
     * Clinical users get their own linked employee UUID, enforced by AppointmentController.
     * Financial accounts do not get access to appointments or client lists.
     */
    public UUID requireSchedulingView(UUID companyId) {
        Company company = companies.requireAccess(companyId);
        User user = currentUser.get();
        if (company.getOwner().getId().equals(user.getId())) return null;
        List<RoleMembership> memberships = jdbc.query(
                "select id, access_role from employees where company_id = ? and account_user_id = ? and active = true",
                (rs, rowNum) -> new RoleMembership((UUID) rs.getObject("id"), rs.getString("access_role")),
                companyId, user.getId());
        if (memberships.stream().anyMatch(m ->
                m.role().equals("COMPANY_ADMIN") || m.role().equals("COMPANY_OWNER") || m.role().equals("RECEPTION"))) {
            return null;
        }
        return memberships.stream().filter(m -> m.role().equals("CLINICAL")).map(RoleMembership::id)
                .findFirst().orElseThrow(() -> new BusinessException("Sem permissão para consultar a agenda clínica."));
    }

    private record RoleMembership(UUID id, String role) { }

    private Company require(UUID companyId, boolean scheduling) {
        Company company = companies.requireAccess(companyId);
        User user = currentUser.get();
        if (company.getOwner().getId().equals(user.getId())) return company;
        String roles = scheduling
                ? "'COMPANY_OWNER','COMPANY_ADMIN','RECEPTION'"
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
