package com.instrua.finance;
import com.instrua.common.exception.BusinessException;
import org.springframework.stereotype.Component;
@Component
public class PaymentStateMachine {
    public enum Status { CREATED, REQUIRES_ACTION, AUTHORIZED, CAPTURED, FAILED, CANCELLED, REFUND_PENDING, REFUNDED }
    public Status transition(Status current, Status target) {
        boolean allowed = switch (current) {
            case CREATED -> target == Status.REQUIRES_ACTION || target == Status.AUTHORIZED || target == Status.FAILED || target == Status.CANCELLED;
            case REQUIRES_ACTION -> target == Status.AUTHORIZED || target == Status.FAILED || target == Status.CANCELLED;
            case AUTHORIZED -> target == Status.CAPTURED || target == Status.CANCELLED || target == Status.FAILED;
            case CAPTURED -> target == Status.REFUND_PENDING;
            case REFUND_PENDING -> target == Status.REFUNDED || target == Status.FAILED;
            case FAILED, CANCELLED, REFUNDED -> false;
        };
        if (!allowed) throw new BusinessException("Transição de pagamento inválida.");
        return target;
    }
}
