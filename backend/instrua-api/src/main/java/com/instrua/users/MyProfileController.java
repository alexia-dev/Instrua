package com.instrua.users;

import com.instrua.common.exception.BusinessException;
import com.instrua.common.exception.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Reusable personal profile owned by the authenticated person.
 * Clinical details are kept separately and NEVER returned by business lookups.
 */
@RestController
@RequestMapping("/api/v1/me/profile")
public class MyProfileController {
    private final CurrentUser currentUser;
    private final JdbcTemplate jdbc;
    public MyProfileController(CurrentUser currentUser, JdbcTemplate jdbc) {
        this.currentUser = currentUser;
        this.jdbc = jdbc;
    }

    @GetMapping
    public Map<String,Object> get() {
        UUID userId = currentUser.get().getId();
        return jdbc.queryForMap("""
            select u.id as user_id, u.name, u.email, p.phone, p.document_number,
                   p.rg, p.birth_date, p.postal_code, p.street, p.street_number,
                   p.complement, p.neighborhood, p.city, p.state,
                   p.emergency_contact_name, p.emergency_contact_phone,
                   coalesce(p.preferred_contact_channel,'NONE') as preferred_contact_channel,
                   coalesce(p.theme,'LIGHT') as theme,
                   coalesce(p.font_scale,1.00) as font_scale,
                   coalesce(p.high_contrast,false) as high_contrast
              from app_users u left join user_profiles p on p.user_id = u.id
             where u.id = ?
            """, userId);
    }

    @PutMapping
    @Transactional
    public Map<String,Object> update(@Valid @RequestBody ProfileRequest r) {
        UUID userId = currentUser.get().getId();
        if (r.birthDate()!=null && r.birthDate().isAfter(LocalDate.now()))
            throw new BusinessException("A data de nascimento não pode estar no futuro.");
        String cpf = trim(r.documentNumber());
        if (cpf!=null && !validCpf(cpf)) throw new BusinessException("Informe um CPF válido.");
        String phone = trim(r.phone());
        if (r.name()!=null && r.name().trim().length()<2)
            throw new BusinessException("Informe seu nome completo.");
        if (r.name()!=null) jdbc.update("update app_users set name = ? where id = ?", r.name().trim(), userId);
        jdbc.update("""
            insert into user_profiles (
                user_id,phone,document_number,rg,birth_date,postal_code,street,street_number,
                complement,neighborhood,city,state,emergency_contact_name,emergency_contact_phone,
                preferred_contact_channel,theme,font_scale,high_contrast,updated_at
            ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now())
            on conflict (user_id) do update set
                phone=excluded.phone, document_number=excluded.document_number, rg=excluded.rg,
                birth_date=excluded.birth_date, postal_code=excluded.postal_code,
                street=excluded.street, street_number=excluded.street_number,
                complement=excluded.complement, neighborhood=excluded.neighborhood,
                city=excluded.city, state=excluded.state,
                emergency_contact_name=excluded.emergency_contact_name,
                emergency_contact_phone=excluded.emergency_contact_phone,
                preferred_contact_channel=excluded.preferred_contact_channel,
                theme=excluded.theme,font_scale=excluded.font_scale,high_contrast=excluded.high_contrast,
                updated_at=now()
            """,
            userId,phone,cpf,trim(r.rg()),r.birthDate()==null?null:Date.valueOf(r.birthDate()),
            trim(r.postalCode()),trim(r.street()),trim(r.streetNumber()),trim(r.complement()),
            trim(r.neighborhood()),trim(r.city()),trim(r.state()),
            trim(r.emergencyContactName()),trim(r.emergencyContactPhone()),
            r.preferredContactChannel()==null?"NONE":r.preferredContactChannel(),
            r.theme()==null?"LIGHT":r.theme(),r.fontScale()==null?BigDecimal.ONE:r.fontScale(),
            Boolean.TRUE.equals(r.highContrast()));
        return get();
    }

    @GetMapping("/private-health")
    public Map<String,Object> privateHealth() {
        UUID id=currentUser.get().getId();
        try {
            return jdbc.queryForMap(
                "select allergies,height_cm,updated_at from user_private_health where user_id = ?",id);
        } catch(EmptyResultDataAccessException ignored) {
            return Map.of("allergies","","height_cm","");
        }
    }

    @PutMapping("/private-health")
    @Transactional
    public Map<String,Object> updatePrivateHealth(@Valid @RequestBody PrivateHealthRequest r) {
        UUID id=currentUser.get().getId();
        jdbc.update("""
            insert into user_private_health(user_id,allergies,height_cm,updated_at)
            values(?,?,?,now())
            on conflict(user_id) do update set allergies=excluded.allergies,
                height_cm=excluded.height_cm,updated_at=now()
            """,id,trim(r.allergies()),r.heightCm());
        return privateHealth();
    }

    @GetMapping("/shares")
    public List<Map<String,Object>> listShares() {
        return jdbc.queryForList("""
            select s.company_id, c.name as company_name, s.permitted, s.updated_at
              from profile_shares s join companies c on c.id=s.company_id
             where s.user_id=? order by c.name
            """, currentUser.get().getId());
    }

    @PutMapping("/shares/{companyId}")
    @Transactional
    public Map<String,Object> changeShare(@PathVariable UUID companyId, @Valid @RequestBody ShareRequest r) {
        Boolean active=jdbc.queryForObject("select exists(select 1 from companies where id=? and active=true)",Boolean.class,companyId);
        if (!Boolean.TRUE.equals(active)) throw new NotFoundException("Estabelecimento não encontrado ou inativo.");
        UUID userId=currentUser.get().getId();
        jdbc.update("""
            insert into profile_shares(user_id,company_id,permitted,updated_at)
            values(?,?,?,now())
            on conflict(user_id,company_id) do update set permitted=excluded.permitted,updated_at=now()
            """,userId,companyId,r.permitted());
        return jdbc.queryForMap("select company_id,permitted,updated_at from profile_shares where user_id=? and company_id=?",userId,companyId);
    }

    public record ShareRequest(boolean permitted) { }
    public record PrivateHealthRequest(@Size(max=2000) String allergies,
        @DecimalMin("20.00") @DecimalMax("300.00") BigDecimal heightCm) { }
    public record ProfileRequest(
        @Size(max=255) String name,
        @Size(max=60) String phone,
        @Size(max=100) String documentNumber,
        @Size(max=30) String rg,
        LocalDate birthDate,
        @Pattern(regexp="^[0-9-]{0,12}$") String postalCode,
        @Size(max=255) String street,
        @Size(max=30) String streetNumber,
        @Size(max=150) String complement,
        @Size(max=150) String neighborhood,
        @Size(max=150) String city,
        @Pattern(regexp="^(|[A-Za-z]{2})$") String state,
        @Size(max=255) String emergencyContactName,
        @Size(max=60) String emergencyContactPhone,
        @Pattern(regexp="^(EMAIL|SMS|WHATSAPP|NONE)?$") String preferredContactChannel,
        @Pattern(regexp="^(LIGHT|DARK)?$") String theme,
        @DecimalMin("0.90") @DecimalMax("1.40") BigDecimal fontScale,
        Boolean highContrast
    ) { }

    private static String trim(String s){return s==null||s.isBlank()?null:s.trim();}
    // CPF digit validation avoids persisting accidental typos. Only the account owner may update it.
    private static boolean validCpf(String value) {
        String s=value.replaceAll("\\D","");
        if(s.length()!=11||s.chars().allMatch(c->c==s.charAt(0)))return false;
        for(int p=9;p<=10;p++){
            int sum=0;
            for(int i=0;i<p;i++)sum+=(s.charAt(i)-'0')*(p+1-i);
            int d=(sum*10)%11;if(d==10)d=0;
            if(d!=s.charAt(p)-'0')return false;
        }
        return true;
    }
}
