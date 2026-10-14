package com.anurag.access.service;

import com.anurag.access.dto.accessrequest.AccessRequestResponse;
import com.anurag.access.dto.accessrequest.CreateAccessRequest;
import com.anurag.access.entity.AccessRequest;
import com.anurag.access.entity.AccessRequestStatus;
import com.anurag.access.entity.Resource;
import com.anurag.access.entity.User;
import com.anurag.access.exception.DuplicateAccessRequestException;
import com.anurag.access.exception.ResourceNotFoundException;
import com.anurag.access.repository.AccessRequestRepository;
import com.anurag.access.repository.ResourceRepository;
import com.anurag.access.repository.UserRepository;
import com.anurag.access.dto.accessrequest.ReviewAccessRequest;
import com.anurag.access.exception.AccessRequestNotFoundException;
import com.anurag.access.exception.InvalidAccessRequestStateException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.anurag.access.dto.common.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccessRequestService {

    private final AccessRequestRepository accessRequestRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final AuditService auditService;

    public AccessRequestService(
            AccessRequestRepository accessRequestRepository,
            UserRepository userRepository,
            ResourceRepository resourceRepository,
            AuditService auditService) {

        this.accessRequestRepository = accessRequestRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.auditService = auditService;
    }

    @Transactional
    public AccessRequestResponse createRequest(
            String requesterEmail,
            CreateAccessRequest request) {

        User requester = userRepository
                .findByEmail(requesterEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Authenticated user not found"
                        )
                );

        Resource resource = resourceRepository
                .findById(request.getResourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: "
                                        + request.getResourceId()
                        )
                );

        if (!resource.isActive()) {
            throw new IllegalStateException(
                    "Access cannot be requested for an inactive resource"
            );
        }

        boolean pendingRequestExists =
                accessRequestRepository
                        .existsByRequesterIdAndResourceIdAndStatus(
                                requester.getId(),
                                resource.getId(),
                                AccessRequestStatus.PENDING
                        );

        if (pendingRequestExists) {
            throw new DuplicateAccessRequestException(
                    "A pending access request already exists "
                            + "for this resource"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        AccessRequest accessRequest = new AccessRequest();

        accessRequest.setRequester(requester);
        accessRequest.setResource(resource);
        accessRequest.setStatus(AccessRequestStatus.PENDING);
        accessRequest.setJustification(
                request.getJustification().trim()
        );
        accessRequest.setCreatedAt(now);
        accessRequest.setUpdatedAt(now);

        AccessRequest saved =
                accessRequestRepository.save(accessRequest);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AccessRequestResponse> getMyRequests(
            String requesterEmail) {

        User requester = userRepository
                .findByEmail(requesterEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Authenticated user not found"
                        )
                );

        return accessRequestRepository
                .findByRequesterIdOrderByCreatedAtDesc(
                        requester.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AccessRequestResponse> getPendingRequests() {

        return accessRequestRepository
                .findByStatusOrderByCreatedAtAsc(
                        AccessRequestStatus.PENDING
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AccessRequestResponse mapToResponse(
            AccessRequest request) {

        Long reviewerId = null;
        String reviewerEmail = null;

        if (request.getReviewedBy() != null) {
            reviewerId = request.getReviewedBy().getId();
            reviewerEmail = request.getReviewedBy().getEmail();
        }

        return new AccessRequestResponse(
                request.getId(),
                request.getRequester().getId(),
                request.getRequester().getEmail(),
                request.getResource().getId(),
                request.getResource().getName(),
                request.getStatus(),
                request.getJustification(),
                reviewerId,
                reviewerEmail,
                request.getReviewComment(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getReviewedAt(),
                request.getVersion()
        );
    }

    @Transactional
    public AccessRequestResponse approveRequest(
            Long requestId,
            String reviewerEmail,
            ReviewAccessRequest review) {

        return reviewRequest(
                requestId,
                reviewerEmail,
                review,
                AccessRequestStatus.APPROVED
        );
    }

    @Transactional
    public AccessRequestResponse rejectRequest(
            Long requestId,
            String reviewerEmail,
            ReviewAccessRequest review) {

        return reviewRequest(
                requestId,
                reviewerEmail,
                review,
                AccessRequestStatus.REJECTED
        );
    }

    private AccessRequestResponse reviewRequest(
            Long requestId,
            String reviewerEmail,
            ReviewAccessRequest review,
            AccessRequestStatus targetStatus) {

        AccessRequest accessRequest = accessRequestRepository
                .findWithDetailsById(requestId)
                .orElseThrow(() -> new AccessRequestNotFoundException(
                        "Access request not found: " + requestId
                ));

        validatePending(accessRequest);

        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new AccessDeniedException(
                        "Reviewer account not found"
                ));

        if (accessRequest.getRequester().getId().equals(reviewer.getId())) {
            throw new AccessDeniedException(
                    "You cannot approve or reject your own request"
            );
        }

        AccessRequestStatus previousStatus = accessRequest.getStatus();
        LocalDateTime now = LocalDateTime.now();

        accessRequest.setStatus(targetStatus);
        accessRequest.setReviewedBy(reviewer);
        accessRequest.setReviewComment(review.comment());
        accessRequest.setReviewedAt(now);
        accessRequest.setUpdatedAt(now);

        accessRequestRepository.flush();
        auditService.recordTransition(
                accessRequest,
                reviewer,
                previousStatus,
                targetStatus,
                review.comment()
        );

        return mapToResponse(accessRequest);
    }

    @Transactional
    public AccessRequestResponse cancelRequest(
            Long requestId,
            String requesterEmail) {

        AccessRequest accessRequest = accessRequestRepository
                .findWithDetailsById(requestId)
                .orElseThrow(() -> new AccessRequestNotFoundException(
                        "Access request not found: " + requestId
                ));

        if (!accessRequest.getRequester().getEmail()
                .equals(requesterEmail)) {

            throw new AccessDeniedException(
                    "You cannot cancel another user's request"
            );
        }

        validatePending(accessRequest);

        AccessRequestStatus previousStatus = accessRequest.getStatus();

        accessRequest.setStatus(AccessRequestStatus.CANCELLED);
        accessRequest.setUpdatedAt(LocalDateTime.now());

        accessRequestRepository.flush();

        auditService.recordTransition(
                accessRequest,
                accessRequest.getRequester(),
                previousStatus,
                AccessRequestStatus.CANCELLED,
                null
        );

        return mapToResponse(accessRequest);
    }

    private void validatePending(AccessRequest accessRequest) {

        if (accessRequest.getStatus() != AccessRequestStatus.PENDING) {
            throw new InvalidAccessRequestStateException(
                    "Only PENDING requests can be modified. Current status: "
                            + accessRequest.getStatus()
            );
        }
    }

    @Transactional(readOnly = true)
    public PagedResponse<AccessRequestResponse> getMyRequestsPaged(
            String requesterEmail,
            AccessRequestStatus status,
            int page,
            int size,
            String sortDirection
    ) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Authenticated user not found"
                        )
                );

        Pageable pageable = buildPageable(page, size, sortDirection);

        Page<AccessRequest> requests;

        if (status == null) {
            requests = accessRequestRepository.findByRequesterId(requester.getId(), pageable);
        } else {
            requests = accessRequestRepository.findByRequesterIdAndStatus(requester.getId(), status, pageable);
        }

        return PagedResponse.from(requests, this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AccessRequestResponse> getPendingRequestsPaged(
            int page,
            int size,
            String sortDirection
    ) {
        Pageable pageable = buildPageable(page, size, sortDirection);

        Page<AccessRequest> requests =
                accessRequestRepository.findByStatus(AccessRequestStatus.PENDING, pageable);

        return PagedResponse.from(requests, this::mapToResponse);
    }

    private Pageable buildPageable(
            int page,
            int size,
            String direction
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Direction must be 'asc' or 'desc'"
            );
        }

        Sort sort = Sort.by(
                sortDirection,
                "createdAt"
        ).and(Sort.by(sortDirection, "id"));

        return PageRequest.of(page, size, sort);
    }
}
