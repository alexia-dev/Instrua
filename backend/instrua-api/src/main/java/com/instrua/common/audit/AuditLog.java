package com.instrua.common.audit;

import com.instrua.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {
    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(length = 8000)
    private String metadata;

    protected AuditLog() { }

    public AuditLog(UUID companyId, UUID actorUserId, String action, String entityType, UUID entityId, String metadata) {
        this.companyId = companyId;
        this.actorUserId = actorUserId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.occurredAt = Instant.now();
        this.metadata = metadata;
    }

    public UUID getCompanyId() { return companyId; }
    public UUID getActorUserId() { return actorUserId; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getMetadata() { return metadata; }
}
