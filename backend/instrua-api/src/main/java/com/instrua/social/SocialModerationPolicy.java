package com.instrua.social;
import com.instrua.common.exception.BusinessException;
import org.springframework.stereotype.Component;
@Component
public class SocialModerationPolicy {
    public enum Status { PENDING, PUBLISHED, HIDDEN, REMOVED }
    public Status moderate(Status current, Status target, boolean canModerate) {
        if (!canModerate || current == null || target == null || current == Status.REMOVED ||
                !(target == Status.PUBLISHED || target == Status.HIDDEN || target == Status.REMOVED))
            throw new BusinessException("Moderação não autorizada ou estado inválido.");
        return target;
    }
}
