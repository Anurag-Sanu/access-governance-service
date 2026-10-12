package com.anurag.access.controller;

import com.anurag.access.dto.audit.AuditLogResponse;
import com.anurag.access.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/access-requests")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<List<AuditLogResponse>> getAuditHistory(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                auditService.getHistoryForCaller(id, authentication)
        );
    }
}
