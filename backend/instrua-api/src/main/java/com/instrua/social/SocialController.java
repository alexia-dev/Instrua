package com.instrua.social;

import com.instrua.companies.CompanyService;
import com.instrua.common.exception.BusinessException;
import com.instrua.common.exception.NotFoundException;
import com.instrua.common.security.DomainAccessService;
import com.instrua.users.Role;
import com.instrua.users.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/social")
public class SocialController {
    private final CurrentUserHolder currentUserHolder;
    private final CompanyService companies;
    private final DomainAccessService access;
    private final JdbcTemplate jdbc;
    private final SocialModerationPolicy policy;
    public SocialController(com.instrua.users.CurrentUser currentUser, CompanyService companies, DomainAccessService access, JdbcTemplate jdbc, SocialModerationPolicy policy) {
        this.currentUserHolder=new CurrentUserHolder(currentUser); this.companies=companies; this.access=access; this.jdbc=jdbc; this.policy=policy;
    }

    @GetMapping("/posts")
    public List<Map<String,Object>> feed() {
        return jdbc.queryForList("select id, author_user_id, company_id, audience, body, created_at from social_posts where status = 'PUBLISHED' and audience = 'PUBLIC' order by created_at desc limit 100");
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createPost(@Valid @RequestBody PostRequest request) {
        User user=access.requireUser(); UUID companyId=request.companyId();
        if(companyId!=null) companies.requireAccess(companyId);
        UUID id=UUID.randomUUID();
        jdbc.update("insert into social_posts (id, author_user_id, company_id, audience, body, status) values (?, ?, ?, ?, ?, 'PENDING')",id,user.getId(),companyId,request.audience().name(),request.body().trim());
        return jdbc.queryForMap("select id, author_user_id, company_id, audience, body, status, created_at from social_posts where id = ?",id);
    }

    @PostMapping("/posts/{postId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> report(@PathVariable UUID postId, @Valid @RequestBody ReportRequest request) {
        User user=access.requireUser();
        Map<String,Object> post;
        try { post=jdbc.queryForMap("select id, author_user_id, status from social_posts where id = ?",postId); }
        catch(org.springframework.dao.EmptyResultDataAccessException ex) { throw new NotFoundException("Publicação não encontrada."); }
        if(user.getId().equals(asUuid(post.get("author_user_id")))) throw new BusinessException("Você não pode denunciar sua própria publicação.");
        UUID id=UUID.randomUUID();
        jdbc.update("insert into social_reports (id, post_id, reporter_user_id, reason_code, details, status) values (?, ?, ?, ?, ?, 'OPEN')",id,postId,user.getId(),request.reasonCode().name(),request.details());
        return jdbc.queryForMap("select id, post_id, reason_code, details, status, created_at from social_reports where id = ?",id);
    }

    @PostMapping("/companies/{companyId}/posts/{postId}/moderation")
    public Map<String,Object> moderate(@PathVariable UUID companyId, @PathVariable UUID postId, @Valid @RequestBody ModerationRequest request) {
        companies.requireAccess(companyId); access.requireCompanyRole(companyId,Set.of(Role.COMPANY_OWNER,Role.COMPANY_ADMIN));
        Map<String,Object> existing;
        try { existing=jdbc.queryForMap("select status from social_posts where id = ? and company_id = ?",postId,companyId); }
        catch(org.springframework.dao.EmptyResultDataAccessException ex) { throw new NotFoundException("Publicação não encontrada neste estabelecimento."); }
        SocialModerationPolicy.Status old=SocialModerationPolicy.Status.valueOf(String.valueOf(existing.get("status")));
        SocialModerationPolicy.Status next=policy.moderate(old,request.status(),true);
        User actor=access.requireUser();
        jdbc.update("update social_posts set status = ?, moderated_by = ?, moderated_at = now(), moderation_reason = ?, updated_at = now() where id = ? and company_id = ?",next.name(),actor.getId(),request.reason(),postId,companyId);
        return jdbc.queryForMap("select id, author_user_id, company_id, audience, body, status, moderated_by, moderated_at, moderation_reason, created_at from social_posts where id = ?",postId);
    }

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> comment(@PathVariable UUID postId, @Valid @RequestBody CommentRequest request) {
        User user=access.requireUser();
        Integer allowed=jdbc.queryForObject("select count(*) from social_posts where id = ? and status = 'PUBLISHED' and audience = 'PUBLIC'",Integer.class,postId);
        if(allowed==null || allowed==0) throw new NotFoundException("Publicação pública não encontrada.");
        UUID id=UUID.randomUUID();
        jdbc.update("insert into social_comments (id, post_id, author_user_id, body, status) values (?, ?, ?, ?, 'PENDING')",id,postId,user.getId(),request.body().trim());
        return jdbc.queryForMap("select id, post_id, author_user_id, body, status, created_at from social_comments where id = ?",id);
    }

    @GetMapping("/posts/{postId}/comments")
    public List<Map<String,Object>> comments(@PathVariable UUID postId) {
        return jdbc.queryForList("select c.id, c.post_id, c.author_user_id, c.body, c.created_at from social_comments c join social_posts p on p.id = c.post_id where p.id = ? and p.status = 'PUBLISHED' and p.audience = 'PUBLIC' and c.status = 'PUBLISHED' order by c.created_at asc limit 500",postId);
    }

    private UUID asUuid(Object value) { return value instanceof UUID u ? u : UUID.fromString(String.valueOf(value)); }
    public enum Audience { PUBLIC, COMPANY }
    public enum ReportReason { SPAM, HARASSMENT, MISINFORMATION, PRIVACY, OTHER }
    public record PostRequest(@Size(max=10000) @NotBlank String body, UUID companyId, @NotNull Audience audience) { }
    public record ReportRequest(@NotNull ReportReason reasonCode, @Size(max=2000) String details) { }
    public record ModerationRequest(@NotNull SocialModerationPolicy.Status status, @Size(max=1000) String reason) { }
    public record CommentRequest(@NotBlank @Size(max=5000) String body) { }
    private record CurrentUserHolder(com.instrua.users.CurrentUser currentUser) { }
}
