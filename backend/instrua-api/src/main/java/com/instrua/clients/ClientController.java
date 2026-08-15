package com.instrua.clients;

import com.instrua.companies.Company;
import com.instrua.companies.CompanyService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/clients")
public class ClientController {
    private final CompanyService companies;
    private final ClientRepository clients;

    public ClientController(CompanyService companies, ClientRepository clients) { this.companies = companies; this.clients = clients; }

    @GetMapping
    public List<ClientResponse> list(@PathVariable UUID companyId) {
        companies.requireAccess(companyId);
        return clients.findAllByCompanyIdOrderByNameAsc(companyId).stream().map(ClientResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse create(@PathVariable UUID companyId, @Valid @RequestBody ClientRequest request) {
        Company company = companies.requireAccess(companyId);
        return ClientResponse.from(clients.save(new Client(company, request.name(), request.email(), request.phone(), request.documentNumber(), request.notes())));
    }

    public record ClientRequest(@NotBlank String name, @Email String email, String phone, String documentNumber, String notes) { }
    public record ClientResponse(UUID id, String name, String email, String phone, String documentNumber, String notes, boolean active) {
        static ClientResponse from(Client client) { return new ClientResponse(client.getId(), client.getName(), client.getEmail(), client.getPhone(), client.getDocumentNumber(), client.getNotes(), client.isActive()); }
    }
}
