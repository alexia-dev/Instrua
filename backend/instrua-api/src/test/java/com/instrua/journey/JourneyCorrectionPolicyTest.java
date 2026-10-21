package com.instrua.journey;
import com.instrua.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class JourneyCorrectionPolicyTest {
    private final JourneyCorrectionPolicy policy = new JourneyCorrectionPolicy();
    @Test void requesterCannotReviewOwnCorrection() {
        assertThrows(BusinessException.class, () -> policy.decide(JourneyCorrectionPolicy.Status.PENDING, JourneyCorrectionPolicy.Status.APPROVED, true, true));
        assertEquals(JourneyCorrectionPolicy.Status.REJECTED, policy.decide(JourneyCorrectionPolicy.Status.PENDING, JourneyCorrectionPolicy.Status.REJECTED, true, false));
    }
}
