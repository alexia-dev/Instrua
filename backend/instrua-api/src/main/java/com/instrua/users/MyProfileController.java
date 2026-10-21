package com.instrua.users;

import com.instrua.common.exception.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.sql.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/me/profile")
public class MyProfileController {
    private final CurrentUser currentUser;
    private final JdbcTemplate jdbc;
    public MyProfileController(CurrentUser currentUser, JdbcTemplate jdbc) { this.currentUser = currentUser; this.jdbc = jdbc; }

    @GetMapping
    public Map<String,Object> get() {
        var user = currentUser.get();
        Map<String,Object> result = jdbc.queryForMap("select u.id as user_id, u.name, u.email, p.phone, p.document_number, p.birth_date, p.preferred_contact_channel from app_users u left join user_profiles p on p.user_id = u.id where u.id = ?", user.getId());
        return result;
    }

    @PutMapping
    public Map<String,Object> update(@Valid @RequestBody ProfileRequest request) {
        var user = currentUser.get();
        jdbc.update("insert into user_profiles (user_id, phone, document_number, birth_date, preferred_contact_channel, updated_at) values (?, ?, ?, ?, ?, now()) on conflict (user_id) do update set phone = excluded.phone, document_number = excluded.document_number, birth_date = excluded.birth_date, preferred_contact_channel = excluded.preferred_contact_channel, updated_at = now()",
                user.getId(), request.phone(), request.documentNumber(), request.birthDate() == null ? null : Date.valueOf(request.birthDate()), request.preferredContactChannel());
        return get();
    }

    public record ProfileRequest(@Size(max=60) String phone, @Size(max=100) String documentNumber, java.time.LocalDate birthDate,
            @Pattern(regexp="^(EMAIL|SMS|WHATSAPP|NONE)?$") String preferredContactChannel) { }
}
