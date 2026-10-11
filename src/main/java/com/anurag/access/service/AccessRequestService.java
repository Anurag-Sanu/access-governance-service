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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccessRequestService {

    private final AccessRequestRepository accessRequestRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    public AccessRequestService(
            AccessRequestRepository accessRequestRepository,
            UserRepository userRepository,
            ResourceRepository resourceRepository) {

        this.accessRequestRepository = accessRequestRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
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
}
