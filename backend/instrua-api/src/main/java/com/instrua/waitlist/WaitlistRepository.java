package com.instrua.waitlist;
import java.util.List; import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.Lock; import org.springframework.data.jpa.repository.Query; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType;
public interface WaitlistRepository extends JpaRepository<WaitlistEntry,UUID> {
 @Query("select w from WaitlistEntry w join fetch w.patient p where w.company.id=:companyId and w.examId=:examId and w.status=:status order by w.priorityLevel desc,w.createdAt asc")
 List<WaitlistEntry> findQueue(@Param("companyId") UUID companyId,@Param("examId") UUID examId,@Param("status") WaitlistStatus status);
 @Query("select w from WaitlistEntry w join fetch w.patient p where w.company.id=:companyId and w.patient.phone=:phone and w.status=com.instrua.waitlist.WaitlistStatus.NOTIFIED order by w.notifiedAt desc")
 List<WaitlistEntry> findNotifiedByPhone(@Param("companyId") UUID companyId,@Param("phone") String phone);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select w from WaitlistEntry w where w.company.id=:companyId and w.examId=:examId and w.status=com.instrua.waitlist.WaitlistStatus.WAITING order by w.priorityLevel desc,w.createdAt asc")
 List<WaitlistEntry> lockNextCandidates(@Param("companyId") UUID companyId,@Param("examId") UUID examId);
}
