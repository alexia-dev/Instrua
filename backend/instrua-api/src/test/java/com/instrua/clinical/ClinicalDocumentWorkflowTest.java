package com.instrua.clinical;
import com.instrua.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ClinicalDocumentWorkflowTest {
    private final ClinicalDocumentWorkflow workflow = new ClinicalDocumentWorkflow();
    @Test void onlyAssignedClinicianCanApproveForIssue() {
        assertThrows(BusinessException.class, () -> workflow.transition(ClinicalDocumentWorkflow.Status.IN_REVIEW, ClinicalDocumentWorkflow.Status.APPROVED_FOR_ISSUE, false, true));
        assertEquals(ClinicalDocumentWorkflow.Status.APPROVED_FOR_ISSUE, workflow.transition(ClinicalDocumentWorkflow.Status.IN_REVIEW, ClinicalDocumentWorkflow.Status.APPROVED_FOR_ISSUE, true, false));
    }
    @Test void deliveredDocumentCannotReturnToReview() {
        assertThrows(BusinessException.class, () -> workflow.transition(ClinicalDocumentWorkflow.Status.DELIVERED, ClinicalDocumentWorkflow.Status.IN_REVIEW, true, false));
    }
}
