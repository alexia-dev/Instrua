package com.instrua.common.audit;

import com.instrua.users.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditService {
    private final AuditLogRepository auditLogs;
    private final CurrentUser currentUser;

    public AuditService(AuditLogRepository auditLogs, CurrentUser currentUser) {
        this.auditLogs = auditLogs;
        this.currentUser = currentUser;
    }

    @Transactional
    public AuditLog record(UUID companyId, String action, String entityType, UUID entityId, String metadata) {
        return auditLogs.save(new AuditLog(companyId, currentUser.get().getId(), action, entityType, entityId, metadata));
    }
}
