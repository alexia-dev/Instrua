package com.instrua.employees;

import com.instrua.companies.Company;
import com.instrua.companies.CompanyRepository;
import com.instrua.common.exception.BusinessException;
import com.instrua.common.exception.NotFoundException;
import com.instrua.common.security.CompanyPermissionService;
import com.instrua.users.CurrentUser;
import com.instrua.users.Role;
import com.instrua.users.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Explicit opt-in membership: the manager issues a one-time invitation,
 * the recipient must sign in with the invited email before accepting.
 * Raw invitation tokens are returned only when created, never stored.
 */
@RestController
@RequestMapping("/api/v1")
public class EmployeeInvitationController {
    private static final Set<Role> INVITABLE = Set.of(Role.RECEPTION, Role.CLINICAL, Role.BILLING, Role.COMPANY_ADMIN);
    private final CompanyPermissionService permissions;
    private final CurrentUser currentUser;
    private final CompanyRepository companies;
    private final EmployeeRepository employees;
    private final JdbcTemplate jdbc;
    private final SecureRandom random = new SecureRandom();

    public EmployeeInvitationController(CompanyPermissionService permissions, CurrentUser currentUser,
            CompanyRepository companies, EmployeeRepository employees, JdbcTemplate jdbc) {
        this.permissions = permissions; this.currentUser = currentUser;
        this.companies = companies; this.employees = employees; this.jdbc = jdbc;
    }

    @PostMapping("/companies/{companyId}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public CreatedInvitation invite(@PathVariable UUID companyId, @Valid @RequestBody InvitationRequest request) {
        permissions.requireManager(companyId);
        if (!INVITABLE.contains(request.role())) throw new BusinessException("Papel de acesso não permitido para convites.");
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        UUID id = UUID.randomUUID();
        Instant expiry = Instant.now().plus(7, ChronoUnit.DAYS);
        jdbc.update("insert into company_invitations (id,company_id,invited_email,proposed_role,token_hash,status,created_by,expires_at) values (?,?,?,?,?,'PENDING',?,?)",
                id,companyId,request.email().trim().toLowerCase(),request.role().name(),hash(token),currentUser.get().getId(),java.sql.Timestamp.from(expiry));
        return new CreatedInvitation(id, token, expiry, "Compartilhe o código somente com a pessoa convidada; nunca envie em grupo público.");
    }

    @GetMapping("/me/invitations/{token}")
    @Transactional(readOnly = true)
    public InvitationDetails details(@PathVariable String token) {
        User user = currentUser.get();
        Map<String,Object> invited = findPendingForUser(token, user);
        Company company = companies.findById((UUID)invited.get("company_id"))
                .orElseThrow(() -> new NotFoundException("Convite não encontrado."));
        return new InvitationDetails(company.getName(),String.valueOf(invited.get("proposed_role")),
                ((java.sql.Timestamp)invited.get("expires_at")).toInstant());
    }

    @PostMapping("/me/invitations/{token}/accept")
    @Transactional
    public AcceptedInvitation accept(@PathVariable String token) {
        User user = currentUser.get();
        Map<String,Object> invitation = findPendingForUserForUpdate(token, user);
        UUID companyId = (UUID)invitation.get("company_id");
        Role role = Role.valueOf(String.valueOf(invitation.get("proposed_role")));
        if (!INVITABLE.contains(role)) throw new BusinessException("Papel inválido.");
        Company company = companies.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Estabelecimento não encontrado."));
        if (!company.isActive()) throw new BusinessException("Estabelecimento inativo.");
        UUID employeeId = jdbc.query(
                "select id from employees where company_id = ? and lower(email) = lower(?) and active = true order by created_at limit 1",
                rs -> rs.next() ? (UUID)rs.getObject(1) : null, companyId, user.getEmail());
        if (employeeId != null) {
            int updated = jdbc.update(
                    "update employees set account_user_id=?,access_role=?,updated_at=now() where id=? and (account_user_id is null or account_user_id=?)",
                    user.getId(), role.name(), employeeId, user.getId());
            if (updated == 0) throw new BusinessException("Este cadastro já pertence a outra conta.");
        } else {
            Employee employee = employees.save(new Employee(company,user,user.getName(),user.getEmail(),null,null,role));
            employeeId = employee.getId();
        }
        jdbc.update("update company_invitations set status='ACCEPTED',responded_at=now(),invited_user_id=? where id=?",
                user.getId(), invitation.get("id"));
        return new AcceptedInvitation(companyId,employeeId,role.name(),"Vínculo criado. Atualize sua área de trabalho.");
    }

    private Map<String,Object> findPendingForUser(String token, User user) {
        try {
            return jdbc.queryForMap(
                    "select id,company_id,proposed_role,expires_at from company_invitations where token_hash=? and lower(invited_email)=lower(?) and status='PENDING' and expires_at>now()",
                    hash(token),user.getEmail());
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Convite inexistente, expirado ou destinado a outra conta.");
        }
    }
    private Map<String,Object> findPendingForUserForUpdate(String token, User user) {
        try {
            return jdbc.queryForMap(
                    "select id,company_id,proposed_role,expires_at from company_invitations where token_hash=? and lower(invited_email)=lower(?) and status='PENDING' and expires_at>now() for update",
                    hash(token),user.getEmail());
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("Convite inexistente, expirado ou destinado a outra conta.");
        }
    }
    private String hash(String token) {
        if (token.length() > 150) throw new NotFoundException("Convite inválido.");
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 indisponível",e); }
    }

    public record InvitationRequest(@Email @NotBlank String email, @NotNull Role role) { }
    public record CreatedInvitation(UUID id, String token, Instant expiresAt, String instruction) { }
    public record InvitationDetails(String companyName, String role, Instant expiresAt) { }
    public record AcceptedInvitation(UUID companyId, UUID employeeId, String role, String message) { }
}
