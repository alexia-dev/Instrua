package com.instrua.waitlist;
import com.instrua.companies.CompanyRepository; import com.instrua.users.CurrentUser; import jakarta.validation.Valid; import java.util.List; import java.util.UUID; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api") public class WaitlistController {
 private final WaitlistService service; private final CompanyRepository companies; private final CurrentUser currentUser;
 public WaitlistController(WaitlistService s,CompanyRepository c,CurrentUser u){service=s;companies=c;currentUser=u;}
 private UUID ensureCompanyAccess(UUID companyId){var user=currentUser.get();return companies.findAccessibleForUser(companyId,user.getId()).orElseThrow(()->new org.springframework.security.access.AccessDeniedException("Sem acesso à clínica")).getId();}
 @PostMapping("/companies/{companyId}/waitlist") @ResponseStatus(HttpStatus.CREATED) public WaitlistDtos.QueueItem join(@PathVariable UUID companyId,@Valid @RequestBody WaitlistDtos.JoinRequest request){var id=ensureCompanyAccess(companyId);var w=service.join(id,request);return new WaitlistDtos.QueueItem(w.getId(),w.getPatient().getId(),w.getPatient().getName(),w.getPatient().getPhone(),w.getExamId(),w.getPriorityLevel(),w.getStatus(),w.getCreatedAt());}
 @GetMapping("/companies/{companyId}/admin/waitlist/{examId}") public List<WaitlistDtos.QueueItem> queue(@PathVariable UUID companyId,@PathVariable UUID examId){return service.queue(ensureCompanyAccess(companyId),examId);}
 @PostMapping("/companies/{companyId}/admin/waitlist/{examId}/call-next") @ResponseStatus(HttpStatus.ACCEPTED) public void callNext(@PathVariable UUID companyId,@PathVariable UUID examId){service.callNext(ensureCompanyAccess(companyId),examId);}
}
