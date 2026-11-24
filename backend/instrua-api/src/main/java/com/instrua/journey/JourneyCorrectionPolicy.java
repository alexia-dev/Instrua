package com.instrua.journey;
import com.instrua.common.exception.BusinessException;
import org.springframework.stereotype.Component;
@Component
public class JourneyCorrectionPolicy {
    public enum Status { PENDING, APPROVED, REJECTED, WITHDRAWN }
    public Status decide(Status current, Status target, boolean actorCanReview, boolean actorIsRequester) {
        if (current != Status.PENDING || (target != Status.APPROVED && target != Status.REJECTED) || !actorCanReview || actorIsRequester)
            throw new BusinessException("Correção de jornada exige revisão por pessoa autorizada diferente do solicitante.");
        return target;
    }
}
