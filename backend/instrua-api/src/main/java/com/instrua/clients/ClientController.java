package com.instrua.clients;

import com.instrua.companies.Company;
import com.instrua.companies.CompanyService;
import com.instrua.common.exception.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/clients")
public class ClientController {
    private final CompanyService companies; private final ClientRepository clients;
    public ClientController(CompanyService companies, ClientRepository clients) { this.companies = companies; this.clients = clients; }
    @GetMapping public List<ClientResponse> list(@PathVariable UUID companyId) { companies.requireAccess(companyId); return clients.findAllByCompanyIdOrderByNameAsc(companyId).stream().map(ClientResponse::from).toList(); }
    @GetMapping("/{clientId}") public ClientResponse get(@PathVariable UUID companyId, @PathVariable UUID clientId) {
        companies.requireAccess(companyId); return ClientResponse.from(clients.findByIdAndCompanyIdAndActiveTrue(clientId, companyId).orElseThrow(() -> new NotFoundException("Ficha não encontrada")));
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse create(@PathVariable UUID companyId, @Valid @RequestBody ClientRequest request) {
        Company company = companies.requireAccess(companyId);
        return ClientResponse.from(clients.save(new Client(company, request.name(), request.email(), request.phone(), request.documentNumber(), request.notes(), request.clientType(), request.legalName(), request.tradeName(), request.contactName(), request.profileData())));
    }
    public record ClientRequest(@NotBlank String name, @Email String email, String phone, String documentNumber, String notes, ClientType clientType, String legalName, String tradeName, String contactName, Map<String,Object> profileData) { }
    public record ClientResponse(UUID id, String name, String email, String phone, String documentNumber, String notes, ClientType clientType, String legalName, String tradeName, String contactName, Map<String,Object> profileData, boolean active) {
        static ClientResponse from(Client c) { return new ClientResponse(c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getDocumentNumber(), c.getNotes(), c.getClientType(), c.getLegalName(), c.getTradeName(), c.getContactName(), c.getProfileData(), c.isActive()); }
    }
}
