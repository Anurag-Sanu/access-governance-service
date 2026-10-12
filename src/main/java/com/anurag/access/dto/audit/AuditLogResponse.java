package com.anurag.access.dto.audit;

import com.anurag.access.entity.AccessRequestStatus;
import com.anurag.access.entity.AuditAction;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long accessRequestId,
        Long actorId,
        String actorEmail,
        AuditAction action,
        AccessRequestStatus previousStatus,
        AccessRequestStatus newStatus,
        String comment,
        LocalDateTime createdAt
) {
}
