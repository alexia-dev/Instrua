package com.instrua.clients;

import com.instrua.common.exception.BusinessException;
import com.instrua.common.exception.NotFoundException;
import com.instrua.common.security.CompanyPermissionService;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Company-local record search and opt-in imports of personal administrative data.
 * Knowing a person's email or telephone never authorizes access to their global account.
 * No allergy, health, clinical record, or unconsented account fields are returned.
 */
@RestController
@RequestMapping("/api/v1/companies/{companyId}/clients")
public class ClientLookupController {
    private final CompanyPermissionService permissions;
    private final JdbcTemplate jdbc;
    public ClientLookupController(CompanyPermissionService permissions, JdbcTemplate jdbc) {
        this.permissions=permissions;this.jdbc=jdbc;
    }

    @GetMapping("/lookup")
    @Transactional(readOnly=true)
    public List<Map<String,Object>> lookup(@PathVariable UUID companyId,@RequestParam("query") String query) {
        permissions.requireScheduling(companyId);
        String term=query==null?"":query.trim();
        if(term.length()<3 || term.length()>100) throw new BusinessException("Informe pelo menos três caracteres do e-mail ou telefone.");
        String escaped=term.toLowerCase(Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_");
        String like="%"+escaped+"%";
        String digits=term.replaceAll("[^0-9]","");
        List<Map<String,Object>> local=jdbc.queryForList("""
            select 'ESTABLISHMENT' as source,id as client_id,linked_user_id as user_id,name,email,phone,
                document_number,rg,birth_date,postal_code,street,street_number,complement,neighborhood,city,state
            from clients
            where company_id=? and active=true and
                (lower(coalesce(email,'')) like ? escape '!' or
                 (? <> '' and regexp_replace(coalesce(phone,''),'[^0-9]','','g') like ?))
            order by name limit 20
            """,companyId,like,digits,"%"+digits+"%");
        List<Map<String,Object>> shared=jdbc.queryForList("""
            select 'AUTHORIZED_ACCOUNT' as source,cast(null as uuid) as client_id,u.id as user_id,u.name,u.email,p.phone,
                p.document_number,p.rg,p.birth_date,p.postal_code,p.street,p.street_number,p.complement,
                p.neighborhood,p.city,p.state
            from profile_shares s
            join app_users u on u.id=s.user_id and u.enabled=true
            left join user_profiles p on p.user_id=u.id
            where s.company_id=? and s.permitted=true and
                (lower(coalesce(u.email,'')) like ? escape '!' or
                 (? <> '' and regexp_replace(coalesce(p.phone,''),'[^0-9]','','g') like ?))
                and not exists(select 1 from clients c where c.company_id=? and c.linked_user_id=u.id and c.active=true)
            order by u.name limit 20
            """,companyId,like,digits,"%"+digits+"%",companyId);
        local.addAll(shared);
        return local.stream().limit(20).toList();
    }

    @GetMapping("/{clientId}/details")
    @Transactional(readOnly=true)
    public Map<String,Object> details(@PathVariable UUID companyId,@PathVariable UUID clientId) {
        permissions.requireScheduling(companyId);
        List<Map<String,Object>> results=jdbc.queryForList("""
            select id as client_id,linked_user_id as user_id,name,email,phone,
                document_number,rg,birth_date,postal_code,street,street_number,complement,
                neighborhood,city,state
            from clients where company_id=? and id=? and active=true
            """,companyId,clientId);
        if(results.isEmpty())throw new NotFoundException("Ficha não encontrada neste estabelecimento.");
        return results.get(0);
    }

    @PostMapping("/import/{userId}")
    @Transactional
    public Map<String,Object> importProfile(@PathVariable UUID companyId,@PathVariable UUID userId) {
        permissions.requireScheduling(companyId);
        List<Map<String,Object>> allowed=jdbc.queryForList("""
            select u.name,u.email,p.phone,p.document_number,p.rg,p.birth_date,p.postal_code,
                p.street,p.street_number,p.complement,p.neighborhood,p.city,p.state
            from profile_shares s
            join app_users u on u.id=s.user_id and u.enabled=true
            left join user_profiles p on p.user_id=u.id
            where s.user_id=? and s.company_id=? and s.permitted=true
            """,userId,companyId);
        if(allowed.isEmpty())throw new BusinessException("Esta pessoa ainda não autorizou o compartilhamento da ficha com o estabelecimento.");
        Map<String,Object> p=allowed.get(0);
        String email=(String)p.get("email");
        List<UUID> found=jdbc.query("""
            select id from clients where company_id=? and active=true
            and (linked_user_id=? or (linked_user_id is null and lower(email)=lower(?)))
            order by case when linked_user_id=? then 0 else 1 end limit 1 for update
            """,(rs,i)->(UUID)rs.getObject("id"),companyId,userId,email,userId);
        UUID id=found.isEmpty()?UUID.randomUUID():found.get(0);
        if(found.isEmpty()) {
            jdbc.update("""
                insert into clients(id,created_at,updated_at,company_id,name,email,phone,document_number,
                    rg,birth_date,postal_code,street,street_number,complement,neighborhood,city,state,linked_user_id,active)
                values (?,now(),now(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,true)
                """,id,companyId,p.get("name"),email,p.get("phone"),p.get("document_number"),
                p.get("rg"),p.get("birth_date"),p.get("postal_code"),p.get("street"),p.get("street_number"),
                p.get("complement"),p.get("neighborhood"),p.get("city"),p.get("state"),userId);
        } else {
            jdbc.update("""
                update clients set name=?,email=?,phone=?,document_number=?,rg=?,birth_date=?,
                    postal_code=?,street=?,street_number=?,complement=?,neighborhood=?,city=?,state=?,
                    linked_user_id=?,updated_at=now()
                where id=? and company_id=?
                """,p.get("name"),email,p.get("phone"),p.get("document_number"),p.get("rg"),
                p.get("birth_date"),p.get("postal_code"),p.get("street"),p.get("street_number"),
                p.get("complement"),p.get("neighborhood"),p.get("city"),p.get("state"),userId,id,companyId);
        }
        return details(companyId,id);
    }
}
