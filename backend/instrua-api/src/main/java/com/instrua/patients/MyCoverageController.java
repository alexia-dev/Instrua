package com.instrua.patients;

import com.instrua.common.exception.BusinessException;
import com.instrua.users.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.sql.Date;
import java.util.*;

@RestController
@RequestMapping("/api/v1/me/coverages")
public class MyCoverageController {
    private final CurrentUser currentUser; private final JdbcTemplate jdbc;
    public MyCoverageController(CurrentUser currentUser, JdbcTemplate jdbc) { this.currentUser=currentUser; this.jdbc=jdbc; }

    @GetMapping
    public List<Map<String,Object>> list() {
        var user=currentUser.get();
        return jdbc.queryForList("select id, payer_name, plan_name, right(member_number, 4) as member_number_last_four, card_holder_name, valid_until, status, verified_at, created_at from patient_coverages where user_id = ? order by created_at desc", user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> create(@Valid @RequestBody CoverageRequest request) {
        var user=currentUser.get(); UUID id=UUID.randomUUID();
        jdbc.update("insert into patient_coverages (id, user_id, payer_name, plan_name, member_number, card_holder_name, valid_until, status) values (?, ?, ?, ?, ?, ?, ?, 'SELF_REPORTED')",
                id,user.getId(),request.payerName().trim(),request.planName()==null?null:request.planName().trim(),request.memberNumber().trim(),request.cardHolderName()==null?null:request.cardHolderName().trim(),request.validUntil()==null?null:Date.valueOf(request.validUntil()));
        return jdbc.queryForMap("select id, payer_name, plan_name, right(member_number, 4) as member_number_last_four, card_holder_name, valid_until, status, verified_at, created_at from patient_coverages where id = ? and user_id = ?", id,user.getId());
    }

    @DeleteMapping("/{coverageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID coverageId) {
        int count=jdbc.update("delete from patient_coverages where id = ? and user_id = ?",coverageId,currentUser.get().getId());
        if(count==0) throw new BusinessException("Cobertura não encontrada na sua conta.");
    }

    public record CoverageRequest(@NotBlank @Size(max=200) String payerName, @Size(max=200) String planName,
            @NotBlank @Size(max=200) String memberNumber, @Size(max=200) String cardHolderName, java.time.LocalDate validUntil) { }
}
