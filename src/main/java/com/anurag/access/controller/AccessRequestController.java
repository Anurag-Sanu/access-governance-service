
package com.anurag.access.controller;

import com.anurag.access.dto.accessrequest.AccessRequestResponse;
import com.anurag.access.dto.accessrequest.CreateAccessRequest;
import com.anurag.access.dto.accessrequest.ReviewAccessRequest;
import com.anurag.access.dto.common.PagedResponse;
import com.anurag.access.entity.AccessRequestStatus;
import com.anurag.access.service.AccessRequestService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PagedResponse<AccessRequestResponse>>
    getMyRequests(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)
            AccessRequestStatus status,
            @RequestParam(defaultValue = "desc") String direction) {

        return ResponseEntity.ok(
                accessRequestService.getMyRequestsPaged(
                        authentication.getName(),
                        status,
                        page,
                        size,
                        direction
                )
        );
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('APPROVER', 'ADMIN')")
    public ResponseEntity<PagedResponse<AccessRequestResponse>>
    getPendingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(
                accessRequestService.getPendingRequestsPaged(
                        page,
                        size,
                        direction
                )
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

