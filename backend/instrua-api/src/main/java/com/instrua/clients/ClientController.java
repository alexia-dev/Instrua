package com.instrua.clients;

import com.instrua.companies.Company;
import com.instrua.common.security.CompanyPermissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/clients")
public class ClientController {
    private final ClientRepository clients;
    private final CompanyPermissionService permissions;
    private final JdbcTemplate jdbc;

    public ClientController(ClientRepository clients,CompanyPermissionService permissions,JdbcTemplate jdbc) {
        this.clients=clients;this.permissions=permissions;this.jdbc=jdbc;
    }

    @GetMapping
    @Transactional(readOnly=true)
    public List<ClientResponse> list(@PathVariable UUID companyId) {
        permissions.requireScheduling(companyId);
        return clients.findAllByCompanyIdOrderByNameAsc(companyId).stream().map(ClientResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ClientResponse create(@PathVariable UUID companyId,@Valid @RequestBody ClientRequest request) {
        Company company=permissions.requireScheduling(companyId);
        Client client=clients.saveAndFlush(new Client(company,request.name(),request.email(),request.phone(),request.documentNumber(),request.notes()));
        jdbc.update("""
            update clients set rg=?,birth_date=?,postal_code=?,street=?,street_number=?,complement=?,
                neighborhood=?,city=?,state=?,updated_at=now()
            where company_id=? and id=?
            """,request.rg(),request.birthDate(),request.postalCode(),request.street(),request.streetNumber(),
            request.complement(),request.neighborhood(),request.city(),request.state(),companyId,client.getId());
        return ClientResponse.from(client);
    }

    public record ClientRequest(
        @NotBlank @Size(max=255) String name,
        @Email @Size(max=255) String email,
        @Size(max=60) String phone,
        @Size(max=100) String documentNumber,
        @Size(max=4000) String notes,
        @Size(max=30) String rg,LocalDate birthDate,
        @Size(max=12) String postalCode,@Size(max=255) String street,
        @Size(max=30) String streetNumber,@Size(max=150) String complement,
        @Size(max=150) String neighborhood,@Size(max=150) String city,@Size(max=2) String state) { }
    public record ClientResponse(UUID id,String name,String email,String phone,String documentNumber,String notes,boolean active) {
        static ClientResponse from(Client c){return new ClientResponse(c.getId(),c.getName(),c.getEmail(),c.getPhone(),c.getDocumentNumber(),c.getNotes(),c.isActive());}
    }
}
