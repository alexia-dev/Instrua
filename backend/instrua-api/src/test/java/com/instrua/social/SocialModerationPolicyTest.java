package com.instrua.social;
import com.instrua.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SocialModerationPolicyTest {
    private final SocialModerationPolicy policy = new SocialModerationPolicy();
    @Test void unauthorizedActorCannotPublishPost() {
        assertThrows(BusinessException.class, () -> policy.moderate(SocialModerationPolicy.Status.PENDING, SocialModerationPolicy.Status.PUBLISHED, false));
        assertEquals(SocialModerationPolicy.Status.PUBLISHED, policy.moderate(SocialModerationPolicy.Status.PENDING, SocialModerationPolicy.Status.PUBLISHED, true));
    }
}
