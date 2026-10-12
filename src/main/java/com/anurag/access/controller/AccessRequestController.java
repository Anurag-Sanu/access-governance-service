package com.anurag.access.controller;

import com.anurag.access.dto.accessrequest.AccessRequestResponse;
import com.anurag.access.dto.accessrequest.CreateAccessRequest;
import com.anurag.access.dto.accessrequest.ReviewAccessRequest;
import com.anurag.access.service.AccessRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/access-requests")
public class AccessRequestController {

    private final AccessRequestService accessRequestService;

    public AccessRequestController(
            AccessRequestService accessRequestService) {

        this.accessRequestService = accessRequestService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AccessRequestResponse> createRequest(
            @Valid @RequestBody CreateAccessRequest request,
            Authentication authentication) {

        AccessRequestResponse response =
                accessRequestService.createRequest(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AccessRequestResponse>>
    getMyRequests(Authentication authentication) {

        return ResponseEntity.ok(
                accessRequestService.getMyRequests(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('APPROVER', 'ADMIN')")
    public ResponseEntity<List<AccessRequestResponse>>
    getPendingRequests() {

        return ResponseEntity.ok(
                accessRequestService.getPendingRequests()
        );
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('APPROVER', 'ADMIN')")
    public ResponseEntity<AccessRequestResponse> approveRequest(
            @PathVariable Long id,
            @Valid @RequestBody ReviewAccessRequest review,
            Authentication authentication) {

        return ResponseEntity.ok(
                accessRequestService.approveRequest(
                        id,
                        authentication.getName(),
                        review
                )
        );
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('APPROVER', 'ADMIN')")
    public ResponseEntity<AccessRequestResponse> rejectRequest(
            @PathVariable Long id,
            @Valid @RequestBody ReviewAccessRequest review,
            Authentication authentication) {

        return ResponseEntity.ok(
                accessRequestService.rejectRequest(
                        id,
                        authentication.getName(),
                        review
                )
        );
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AccessRequestResponse> cancelRequest(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                accessRequestService.cancelRequest(
                        id,
                        authentication.getName()
                )
        );
    }
}
