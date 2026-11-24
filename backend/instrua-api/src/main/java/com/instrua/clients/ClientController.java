package com.instrua.clients;

import com.instrua.companies.Company;
import com.instrua.companies.CompanyService;
import com.instrua.common.exception.BusinessException;
import com.instrua.common.exception.NotFoundException;
import com.instrua.common.security.CompanyPermissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * One private client file per company. A global personal profile is visible only
 * after its owner explicitly grants this particular company permission.
 */
@RestController
@RequestMapping("/api/v1/companies/{companyId}/clients")
public class ClientController {
    private final CompanyService companies;
    private final ClientRepository clients;
    private final CompanyPermissionService permissions;
    private final JdbcTemplate jdbc;

    public ClientController(CompanyService companies, ClientRepository clients, CompanyPermissionService permissions, JdbcTemplate jdbc) {
        this.companies=companies;this.clients=clients;this.permissions=permissions;this.jdbc=jdbc;
    }

    @GetMapping
    public List<ClientResponse> list(@PathVariable UUID companyId) {
        permissions.requireScheduling(companyId);
        return clients.findAllByCompanyIdOrderByNameAsc(companyId).stream().map(ClientResponse::from).toList();
    }

    @GetMapping("/{clientId}")
    public ClientResponse detail(@PathVariable UUID companyId, @PathVariable UUID clientId) {
        permissions.requireScheduling(companyId);
        return ClientResponse.from(clients.findByIdAndCompanyIdAndActiveTrue(clientId,companyId)
            .orElseThrow(() -> new NotFoundException("Ficha não encontrada neste estabelecimento.")));
    }

    @GetMapping("/lookup")
    public List<Map<String,Object>> lookup(@PathVariable UUID companyId,
                        @RequestParam(required=false) String email,
                        @RequestParam(required=false) String phone) {
        permissions.requireScheduling(companyId);
        String e=email==null?"":email.trim().toLowerCase();
        String p=digits(phone);
        if(e.isBlank()&&p.isBlank())throw new BusinessException("Informe o e-mail ou telefone completo.");
        if(!e.isBlank() && (e.length()<5 || e.length()>255 || !e.contains("@")))
            throw new BusinessException("Digite um e-mail completo para buscar.");
        if(e.isBlank() && p.length()<10)throw new BusinessException("Digite o telefone com DDD para buscar.");
        List<Map<String,Object>> found=new ArrayList<>();
        List<Map<String,Object>> local=e.isBlank()
            ? jdbc.queryForList("select id, name, email, phone, document_number, rg, birth_date, postal_code, street, street_number, complement, neighborhood, city, state, linked_user_id from clients where company_id=? and active=true and regexp_replace(coalesce(phone,''),'[^0-9]','','g')=? order by name limit 15",companyId,p)
            : jdbc.queryForList("select id, name, email, phone, document_number, rg, birth_date, postal_code, street, street_number, complement, neighborhood, city, state, linked_user_id from clients where company_id=? and active=true and lower(email)=? order by name limit 15",companyId,e);
        for(Map<String,Object> row:local){
            Map<String,Object> result=new LinkedHashMap<>(row);result.put("source","LOCAL");
            found.add(result);
        }
        // Never query the global profiles of people who did not consent to this company.
        List<Map<String,Object>> shared=e.isBlank()
            ? jdbc.queryForList("""
              select u.id as user_id, u.name, u.email, p.phone, p.document_number, p.rg,
                p.birth_date, p.postal_code, p.street, p.street_number, p.complement,
                p.neighborhood, p.city, p.state
                from profile_shares sh join app_users u on u.id=sh.user_id
                left join user_profiles p on p.user_id=u.id
               where sh.company_id=? and sh.permitted=true and u.enabled=true
                 and regexp_replace(coalesce(p.phone,''),'[^0-9]','','g')=?
               order by u.name limit 15
              """,companyId,p)
            : jdbc.queryForList("""
              select u.id as user_id, u.name, u.email, p.phone, p.document_number, p.rg,
                p.birth_date, p.postal_code, p.street, p.street_number, p.complement,
                p.neighborhood, p.city, p.state
                from profile_shares sh join app_users u on u.id=sh.user_id
                left join user_profiles p on p.user_id=u.id
               where sh.company_id=? and sh.permitted=true and u.enabled=true and lower(u.email)=?
               order by u.name limit 15
              """,companyId,e);
        for(Map<String,Object> row:shared){
            Map<String,Object> result=new LinkedHashMap<>(row);result.put("source","SHARED");
            // Private health details are never part of a reusable administrative client file.
            found.add(result);
        }
        return found;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ClientResponse create(@PathVariable UUID companyId, @Valid @RequestBody ClientRequest request) {
        Company company=permissions.requireScheduling(companyId);
        Client client=new Client(company,request.name().trim(),trim(request.email()),trim(request.phone()),trim(request.documentNumber()),trim(request.notes()));
        client.fillAddress(trim(request.rg()),request.birthDate(),trim(request.postalCode()),trim(request.street()),trim(request.streetNumber()),
                trim(request.complement()),trim(request.neighborhood()),trim(request.city()),trim(request.state()));
        return ClientResponse.from(clients.save(client));
    }

    @PutMapping("/{clientId}")
    @Transactional
    public ClientResponse update(@PathVariable UUID companyId, @PathVariable UUID clientId, @Valid @RequestBody ClientRequest r) {
        permissions.requireScheduling(companyId);
        Client client=clients.findByIdAndCompanyIdAndActiveTrue(clientId,companyId)
            .orElseThrow(() -> new NotFoundException("Ficha não encontrada neste estabelecimento."));
        client.updateContact(r.name(),trim(r.email()),trim(r.phone()),trim(r.documentNumber()),trim(r.notes()));
        client.fillAddress(trim(r.rg()),r.birthDate(),trim(r.postalCode()),trim(r.street()),trim(r.streetNumber()),
            trim(r.complement()),trim(r.neighborhood()),trim(r.city()),trim(r.state()));
        return ClientResponse.from(clients.save(client));
    }

    @PostMapping("/import/{userId}")
    @Transactional
    public ClientResponse importShared(@PathVariable UUID companyId, @PathVariable UUID userId) {
        Company company=permissions.requireScheduling(companyId);
        Map<String,Object> p;
        try{
            p=jdbc.queryForMap("""
               select u.name,u.email,p.phone,p.document_number,p.rg,p.birth_date,
                 p.postal_code,p.street,p.street_number,p.complement,p.neighborhood,p.city,p.state
                 from profile_shares sh join app_users u on u.id=sh.user_id
                 left join user_profiles p on p.user_id=u.id
                where sh.company_id=? and sh.user_id=? and sh.permitted=true and u.enabled=true
                """,companyId,userId);
        }catch(EmptyResultDataAccessException ex){
            throw new BusinessException("A pessoa ainda não autorizou este estabelecimento a utilizar sua ficha.");
        }
        var linked=jdbc.queryForList("select id from clients where company_id=? and linked_user_id=? and active=true limit 1",UUID.class,companyId,userId);
        Client client;
        if(!linked.isEmpty())client=clients.findByIdAndCompanyIdAndActiveTrue(linked.get(0),companyId)
            .orElseThrow(() -> new NotFoundException("Ficha vinculada indisponível."));
        else client=new Client(company,(String)p.get("name"),(String)p.get("email"),(String)p.get("phone"),(String)p.get("document_number"),null);
        client.updateContact((String)p.get("name"),(String)p.get("email"),(String)p.get("phone"),(String)p.get("document_number"),client.getNotes());
        client.fillAddress((String)p.get("rg"),p.get("birth_date")==null?null:((java.sql.Date)p.get("birth_date")).toLocalDate(),
            (String)p.get("postal_code"),(String)p.get("street"),(String)p.get("street_number"),
            (String)p.get("complement"),(String)p.get("neighborhood"),(String)p.get("city"),(String)p.get("state"));
        client.linkAccount(userId);
        return ClientResponse.from(clients.save(client));
    }

    public record ClientRequest(@NotBlank @Size(max=255) String name, @Email String email, @Size(max=60) String phone,
        @Size(max=100) String documentNumber, @Size(max=4000) String notes,
        @Size(max=30) String rg, LocalDate birthDate, @Size(max=12) String postalCode,
        @Size(max=255) String street, @Size(max=30) String streetNumber, @Size(max=150) String complement,
        @Size(max=150) String neighborhood, @Size(max=150) String city, @Size(max=2) String state) { }

    public record ClientResponse(UUID id, String name, String email, String phone,
            String documentNumber, String notes, boolean active,
            String rg, LocalDate birthDate, String postalCode, String street,
            String streetNumber, String complement, String neighborhood, String city,
            String state, UUID linkedUserId) {
        static ClientResponse from(Client c){
            return new ClientResponse(c.getId(),c.getName(),c.getEmail(),c.getPhone(),
                c.getDocumentNumber(),c.getNotes(),c.isActive(),c.getRg(),c.getBirthDate(),
                c.getPostalCode(),c.getStreet(),c.getStreetNumber(),c.getComplement(),
                c.getNeighborhood(),c.getCity(),c.getState(),c.getLinkedUserId());
        }
    }
    private static String trim(String s){return s==null||s.isBlank()?null:s.trim();}
    private static String digits(String s){return s==null?"":s.replaceAll("[^0-9]","");}
}
