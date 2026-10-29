package com.instrua.finance;
import com.instrua.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PaymentStateMachineTest {
    private final PaymentStateMachine machine = new PaymentStateMachine();
    @Test void capturedPaymentMayOnlyStartRefund() {
        assertEquals(PaymentStateMachine.Status.REFUND_PENDING, machine.transition(PaymentStateMachine.Status.CAPTURED, PaymentStateMachine.Status.REFUND_PENDING));
        assertThrows(BusinessException.class, () -> machine.transition(PaymentStateMachine.Status.CAPTURED, PaymentStateMachine.Status.CANCELLED));
    }
}
