package com.instrua.appointments;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Snapshot that should be persisted at booking confirmation and never rewritten by later policy edits. */
public record CancellationPolicySnapshot(UUID policyId, int version, String title, String policyText,
        BigDecimal depositAmount, Integer cancellationDeadlineMinutes, BigDecimal lateCancellationFee,
        BigDecimal noShowFee, String currency, Instant acceptedAt, String acceptanceChannel) { }
