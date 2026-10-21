package com.instrua.clinical;
import com.instrua.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import java.util.Set;

/** Upload or scan does not imply that a document is an electronically signed original. */
@Component
public class ClinicalDocumentWorkflow {
    public enum Status { REQUESTED, TRIAGED, IN_REVIEW, APPROVED_FOR_ISSUE, REJECTED, ISSUED, DELIVERED, CANCELLED }
    public Status transition(Status current, Status target, boolean actorIsAssignedClinician, boolean actorCanManageRequests) {
        if (current == null || target == null) throw new BusinessException("Estado de documento inválido.");
        boolean allowed = switch (current) {
            case REQUESTED -> target == Status.TRIAGED || target == Status.CANCELLED;
            case TRIAGED -> target == Status.IN_REVIEW || target == Status.CANCELLED;
            case IN_REVIEW -> (target == Status.APPROVED_FOR_ISSUE || target == Status.REJECTED) && actorIsAssignedClinician;
            case APPROVED_FOR_ISSUE -> target == Status.ISSUED && actorIsAssignedClinician;
            case ISSUED -> target == Status.DELIVERED;
            case REJECTED, DELIVERED, CANCELLED -> false;
        };
        if (!allowed || !(actorIsAssignedClinician || actorCanManageRequests))
            throw new BusinessException("Transição não permitida. A decisão clínica cabe ao profissional responsável.");
        return target;
    }
    public Set<String> requiredProvenanceForScannedPaper() {
        return Set.of("documentKind", "originalSeenBy", "scannedAt", "storageKey", "isScanCopy");
    }
}
