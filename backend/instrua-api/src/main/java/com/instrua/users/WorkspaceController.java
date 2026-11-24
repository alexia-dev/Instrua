package com.instrua.users;

import com.instrua.companies.Company;
import com.instrua.companies.CompanyService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resolve each person's effective role for each establishment from persisted membership.
 * The UI must not infer a reception, billing, clinical or manager role from a global JWT role.
 */
@RestController
@RequestMapping("/api/v1/me")
public class WorkspaceController {
    private final CompanyService companies;
    private final CurrentUser currentUser;
    private final JdbcTemplate jdbc;

    public WorkspaceController(CompanyService companies, CurrentUser currentUser, JdbcTemplate jdbc) {
        this.companies = companies;
        this.currentUser = currentUser;
        this.jdbc = jdbc;
    }

    @GetMapping("/workspaces")
    @Transactional(readOnly = true)
    public List<WorkspaceRole> list() {
        User user = currentUser.get();
        if (user.getRoles().contains(Role.PLATFORM_ADMIN)) return List.of();
        List<WorkspaceRole> result = new ArrayList<>();
        for (Company company : companies.accessible()) {
            if (company.getOwner().getId().equals(user.getId())) {
                result.add(new WorkspaceRole(company.getId(), "COMPANY_OWNER", null));
                continue;
            }
            List<WorkspaceRole> memberships = jdbc.query(
                "select id, access_role from employees where company_id = ? and account_user_id = ? and active = true order by created_at limit 1",
                (rs, rowNum) -> new WorkspaceRole(company.getId(), rs.getString("access_role"), (UUID) rs.getObject("id")),
                company.getId(), user.getId());
            result.addAll(memberships);
        }
        return result;
    }

    public record WorkspaceRole(UUID companyId, String role, UUID employeeId) {}
}
