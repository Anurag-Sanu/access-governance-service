package com.anurag.access.service;

import com.anurag.access.dto.audit.AuditLogResponse;
import com.anurag.access.entity.*;
import com.anurag.access.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.anurag.access.exception.AccessRequestNotFoundException;
import com.anurag.access.repository.AccessRequestRepository;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AccessRequestRepository accessRequestRepository;

    public AuditService(
            AuditLogRepository auditLogRepository,
            AccessRequestRepository accessRequestRepository) {

        this.auditLogRepository = auditLogRepository;
        this.accessRequestRepository = accessRequestRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordTransition(
            AccessRequest accessRequest,
            User actor,
            AccessRequestStatus previousStatus,
            AccessRequestStatus newStatus,
            String comment) {

        AuditAction action = AuditAction.valueOf(newStatus.name());

        AuditLog auditLog = new AuditLog(
                accessRequest,
                actor,
                action,
                previousStatus,
                newStatus,
                comment,
                LocalDateTime.now()
        );

        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getHistory(Long requestId) {

        return auditLogRepository
                .findByAccessRequestIdOrderByCreatedAtAscIdAsc(requestId)
                .stream()
                .map(log -> new AuditLogResponse(
                        log.getId(),
                        log.getAccessRequest().getId(),
                        log.getActor().getId(),
                        log.getActor().getEmail(),
                        log.getAction(),
                        log.getPreviousStatus(),
                        log.getNewStatus(),
                        log.getComment(),
                        log.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getHistoryForCaller(
            Long requestId,
            Authentication authentication) {

        AccessRequest accessRequest = accessRequestRepository
                .findWithDetailsById(requestId)
                .orElseThrow(() -> new AccessRequestNotFoundException(
                        "Access request not found: " + requestId
                ));

        boolean isOwner = accessRequest.getRequester()
                .getEmail()
                .equals(authentication.getName());

        boolean isReviewer = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_APPROVER")
                                || authority.getAuthority().equals("ROLE_ADMIN")
                );

        if (!isOwner && !isReviewer) {
            throw new AccessRequestNotFoundException(
                    "Access request not found: " + requestId
            );
        }

        return getHistory(requestId);
    }
}
