package com.instrua.users;

import com.instrua.common.exception.BusinessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/profile")
public class MyProfileController {
    private final CurrentUser currentUser;
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate named;
    public MyProfileController(CurrentUser currentUser, JdbcTemplate jdbc, NamedParameterJdbcTemplate named) {
        this.currentUser = currentUser;
        this.jdbc = jdbc;
        this.named = named;
    }

    @GetMapping
    public Map<String,Object> get() {
        UUID userId = currentUser.get().getId();
        return jdbc.queryForMap("""
            select u.id as user_id, u.name, u.email, p.phone, p.document_number, p.birth_date,
            p.preferred_contact_channel, p.rg, p.postal_code, p.street, p.street_number,
            p.complement, p.neighborhood, p.city, p.state, p.emergency_contact_name,
            p.emergency_contact_phone, coalesce(p.theme, 'LIGHT') as theme,
            coalesce(p.font_scale, 1.00) as font_scale, coalesce(p.high_contrast, false) as high_contrast
            from app_users u left join user_profiles p on p.user_id=u.id where u.id=?
            """, userId);
    }

    @PutMapping
    @Transactional
    public Map<String,Object> update(@Valid @RequestBody ProfileRequest request) {
        UUID id = currentUser.get().getId();
        String cpf = blankToNull(request.documentNumber());
        if (cpf != null && !cpf.matches("\\d{11}")) throw new BusinessException("Informe o CPF com 11 dígitos, sem pontuação.");
        if (request.fullName() != null && !request.fullName().isBlank()) {
            jdbc.update("update app_users set name=?,updated_at=now() where id=?", request.fullName().trim(), id);
        }
        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("id",id).addValue("phone",blankToNull(request.phone()))
            .addValue("cpf",cpf).addValue("birth",request.birthDate()==null?null:Date.valueOf(request.birthDate()))
            .addValue("channel",blankToNull(request.preferredContactChannel()))
            .addValue("rg",blankToNull(request.rg()))
            .addValue("postal",blankToNull(request.postalCode()))
            .addValue("street",blankToNull(request.street()))
            .addValue("number",blankToNull(request.streetNumber()))
            .addValue("complement",blankToNull(request.complement()))
            .addValue("neighborhood",blankToNull(request.neighborhood()))
            .addValue("city",blankToNull(request.city()))
            .addValue("state",blankToNull(request.state())==null?null:request.state().toUpperCase())
            .addValue("emergencyName",blankToNull(request.emergencyContactName()))
            .addValue("emergencyPhone",blankToNull(request.emergencyContactPhone()))
            .addValue("theme",request.theme()).addValue("scale",request.fontScale())
            .addValue("contrast",request.highContrast());
        named.update("""
            insert into user_profiles (user_id,phone,document_number,birth_date,preferred_contact_channel,
                rg,postal_code,street,street_number,complement,neighborhood,city,state,
                emergency_contact_name,emergency_contact_phone,theme,font_scale,high_contrast,updated_at)
            values (:id,:phone,:cpf,:birth,:channel,:rg,:postal,:street,:number,:complement,:neighborhood,
                :city,:state,:emergencyName,:emergencyPhone,:theme,:scale,:contrast,now())
            on conflict (user_id) do update set
                phone=excluded.phone,document_number=excluded.document_number,birth_date=excluded.birth_date,
                preferred_contact_channel=excluded.preferred_contact_channel,rg=excluded.rg,
                postal_code=excluded.postal_code,street=excluded.street,street_number=excluded.street_number,
                complement=excluded.complement,neighborhood=excluded.neighborhood,city=excluded.city,
                state=excluded.state,emergency_contact_name=excluded.emergency_contact_name,
                emergency_contact_phone=excluded.emergency_contact_phone,theme=excluded.theme,
                font_scale=excluded.font_scale,high_contrast=excluded.high_contrast,updated_at=now()
            """,p);
        return get();
    }

    // Medical details are self-managed and are NEVER included in administrative profile sharing.
    @GetMapping("/health")
    public Map<String,Object> health() {
        UUID id=currentUser.get().getId();
        return jdbc.query("select allergies,height_cm from user_private_health where user_id=?",
                rs -> rs.next()?Map.<String,Object>of("allergies",rs.getString("allergies")==null?"":rs.getString("allergies"),
                    "heightCm",rs.getBigDecimal("height_cm")==null?"":rs.getBigDecimal("height_cm")):
                    Map.<String,Object>of("allergies","","heightCm",""),id);
    }
    @PutMapping("/health")
    @Transactional
    public Map<String,Object> updateHealth(@Valid @RequestBody HealthRequest request) {
        UUID id=currentUser.get().getId();
        jdbc.update("insert into user_private_health(user_id,allergies,height_cm) values (?,?,?) on conflict (user_id) do update set allergies=excluded.allergies,height_cm=excluded.height_cm,updated_at=now()",
                id,blankToNull(request.allergies()),request.heightCm());
        return health();
    }

    @GetMapping("/shares")
    public List<Map<String,Object>> shares() {
        return jdbc.queryForList("""
            select s.company_id,c.name as company_name,s.permitted,s.updated_at
            from profile_shares s join companies c on c.id=s.company_id
            where s.user_id=? and c.active=true order by c.name
            """,currentUser.get().getId());
    }

    @PutMapping("/shares/{companyId}")
    @Transactional
    public Map<String,Object> share(@PathVariable UUID companyId,@Valid @RequestBody ShareRequest request) {
        UUID userId=currentUser.get().getId();
        Boolean active=jdbc.queryForObject("select exists(select 1 from companies where id=? and active=true)",Boolean.class,companyId);
        if(!Boolean.TRUE.equals(active))throw new BusinessException("Estabelecimento indisponível.");
        jdbc.update("""
            insert into profile_shares(user_id,company_id,permitted) values(?,?,?)
            on conflict(user_id,company_id) do update set permitted=excluded.permitted,updated_at=now()
            """,userId,companyId,request.permitted());
        jdbc.update("""
            insert into audit_logs(id,company_id,actor_user_id,action,entity_type,entity_id,occurred_at,created_at,updated_at,metadata)
            values(?,?,?,'PROFILE_SHARING_CHANGED','USER_PROFILE',?,now(),now(),now(),?)
            """,UUID.randomUUID(),companyId,userId,userId,request.permitted()?"GRANTED":"REVOKED");
        return Map.of("companyId",companyId,"permitted",request.permitted());
    }

    private static String blankToNull(String value) { return value==null||value.isBlank()?null:value.trim(); }

    public record ProfileRequest(
        @Size(min=2,max=255) String fullName,@Size(max=60) String phone,
        @Size(max=100) String documentNumber,LocalDate birthDate,
        @Pattern(regexp="^(EMAIL|SMS|WHATSAPP|NONE)?$") String preferredContactChannel,
        @Size(max=30) String rg,@Size(max=12) String postalCode,@Size(max=255) String street,
        @Size(max=30) String streetNumber,@Size(max=150) String complement,
        @Size(max=150) String neighborhood,@Size(max=150) String city,
        @Pattern(regexp="^([A-Za-z]{2})?$") String state,
        @Size(max=255) String emergencyContactName,@Size(max=60) String emergencyContactPhone,
        @NotNull @Pattern(regexp="^(LIGHT|DARK)$") String theme,
        @NotNull @DecimalMin("0.90") @DecimalMax("1.40") BigDecimal fontScale,
        @NotNull Boolean highContrast
    ) { }
    public record ShareRequest(@NotNull Boolean permitted) { }
    public record HealthRequest(@Size(max=2000) String allergies,@DecimalMin("20") @DecimalMax("300") BigDecimal heightCm) { }
}
