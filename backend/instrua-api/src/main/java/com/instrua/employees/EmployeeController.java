package com.instrua.employees;

import com.instrua.companies.Company;
import com.instrua.companies.CompanyService;
import com.instrua.common.exception.BusinessException;
import com.instrua.common.exception.NotFoundException;
import com.instrua.users.Role;
import com.instrua.users.User;
import com.instrua.users.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/employees")
@Transactional
public class EmployeeController {
    private final CompanyService companies;
    private final EmployeeRepository employees;
    private final UserRepository users;

    public EmployeeController(CompanyService companies, EmployeeRepository employees, UserRepository users) {
        this.companies = companies;
        this.employees = employees;
        this.users = users;
    }

    @GetMapping
    public List<EmployeeResponse> list(@PathVariable UUID companyId) {
        companies.requireAccess(companyId);
        return employees.findAllByCompanyIdOrderByNameAsc(companyId).stream().map(EmployeeResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@PathVariable UUID companyId, @Valid @RequestBody EmployeeRequest request) {
        Company company = companies.requireAccess(companyId);
        User account = null;
        if (request.accountUserId() != null) {
            account = users.findById(request.accountUserId()).orElseThrow(() -> new NotFoundException("Conta de usuário não encontrada"));
            if (employees.existsByCompanyIdAndAccountId(companyId, account.getId())) throw new BusinessException("Esta conta já pertence à empresa");
        }
        return EmployeeResponse.from(employees.save(new Employee(company, account, request.name(), request.email(), request.phone(), request.title(), request.accessRole())));
    }

    public record EmployeeRequest(@NotBlank String name, @Email String email, String phone, String title, Role accessRole, UUID accountUserId) { }
    public record EmployeeResponse(UUID id, UUID accountUserId, String name, String email, String phone, String title, Role accessRole, boolean active) {
        static EmployeeResponse from(Employee employee) { return new EmployeeResponse(employee.getId(), employee.getAccount() == null ? null : employee.getAccount().getId(), employee.getName(), employee.getEmail(), employee.getPhone(), employee.getTitle(), employee.getAccessRole(), employee.isActive()); }
    }
}
