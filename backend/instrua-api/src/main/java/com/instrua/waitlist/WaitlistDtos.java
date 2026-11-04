package com.instrua.waitlist;
import jakarta.validation.constraints.*; import java.util.UUID;
public final class WaitlistDtos { private WaitlistDtos(){} public record JoinRequest(@NotNull UUID patientId,@NotNull UUID examId,@Size(max=1000) String medicalRequestUrl,@Size(max=100) String preferredShifts,@Min(0) @Max(2) int priorityLevel){} public record QueueItem(UUID id,UUID patientId,String patientName,String phone,UUID examId,int priorityLevel,WaitlistStatus status,java.time.Instant createdAt){} }
