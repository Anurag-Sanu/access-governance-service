package com.anurag.access.service;

import com.anurag.access.dto.accessrequest.AccessRequestResponse;
import com.anurag.access.dto.accessrequest.ReviewAccessRequest;
import com.anurag.access.entity.*;
import com.anurag.access.exception.InvalidAccessRequestStateException;
import com.anurag.access.repository.AccessRequestRepository;
import com.anurag.access.repository.ResourceRepository;
import com.anurag.access.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessRequestServiceTest {

    @Mock
    private AccessRequestRepository accessRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AccessRequestService accessRequestService;

    private User requester;
    private User reviewer;
    private Resource resource;
    private AccessRequest accessRequest;

    @BeforeEach
    void setUp() {

        requester = new User();
        requester.setId(1L);
        requester.setEmail("user@example.com");

        reviewer = new User();
        reviewer.setId(2L);
        reviewer.setEmail("approver@example.com");

        resource = new Resource();
        resource.setId(10L);
        resource.setName("Customer Database");

        accessRequest = new AccessRequest();
        accessRequest.setId(100L);
        accessRequest.setRequester(requester);
        accessRequest.setResource(resource);
        accessRequest.setStatus(AccessRequestStatus.PENDING);
        accessRequest.setJustification("Need database access");
        accessRequest.setCreatedAt(LocalDateTime.now());
        accessRequest.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void shouldApprovePendingRequestAndWriteAudit() {

        when(accessRequestRepository.findWithDetailsById(100L))
                .thenReturn(Optional.of(accessRequest));

        when(userRepository.findByEmail("approver@example.com"))
                .thenReturn(Optional.of(reviewer));

        ReviewAccessRequest review =
                new ReviewAccessRequest("Approved for support");

        AccessRequestResponse response =
                accessRequestService.approveRequest(
                        100L,
                        "approver@example.com",
                        review
                );

        assertThat(response.status())
                .isEqualTo(AccessRequestStatus.APPROVED);

        assertThat(accessRequest.getReviewedBy())
                .isEqualTo(reviewer);

        assertThat(accessRequest.getReviewedAt())
                .isNotNull();

        verify(accessRequestRepository).flush();

        verify(auditService).recordTransition(
                accessRequest,
                reviewer,
                AccessRequestStatus.PENDING,
                AccessRequestStatus.APPROVED,
                "Approved for support"
        );
    }

    @Test
    void shouldRejectPendingRequestAndWriteAudit() {

        when(accessRequestRepository.findWithDetailsById(100L))
                .thenReturn(Optional.of(accessRequest));

        when(userRepository.findByEmail("approver@example.com"))
                .thenReturn(Optional.of(reviewer));

        ReviewAccessRequest review =
                new ReviewAccessRequest("Insufficient justification");

        AccessRequestResponse response =
                accessRequestService.rejectRequest(
                        100L,
                        "approver@example.com",
                        review
                );

        assertThat(response.status())
                .isEqualTo(AccessRequestStatus.REJECTED);

        verify(auditService).recordTransition(
                accessRequest,
                reviewer,
                AccessRequestStatus.PENDING,
                AccessRequestStatus.REJECTED,
                "Insufficient justification"
        );
    }

    @Test
    void shouldCancelOwnPendingRequestAndWriteAudit() {

        when(accessRequestRepository.findWithDetailsById(100L))
                .thenReturn(Optional.of(accessRequest));

        AccessRequestResponse response =
                accessRequestService.cancelRequest(
                        100L,
                        "user@example.com"
                );

        assertThat(response.status())
                .isEqualTo(AccessRequestStatus.CANCELLED);

        verify(auditService).recordTransition(
                accessRequest,
                requester,
                AccessRequestStatus.PENDING,
                AccessRequestStatus.CANCELLED,
                null
        );
    }

    @Test
    void shouldNotApproveAlreadyApprovedRequest() {

        accessRequest.setStatus(AccessRequestStatus.APPROVED);

        when(accessRequestRepository.findWithDetailsById(100L))
                .thenReturn(Optional.of(accessRequest));

        assertThatThrownBy(() ->
                accessRequestService.approveRequest(
                        100L,
                        "approver@example.com",
                        new ReviewAccessRequest("Approved")
                )
        ).isInstanceOf(InvalidAccessRequestStateException.class);

        verifyNoInteractions(auditService);
        verify(accessRequestRepository, never()).flush();
    }

    @Test
    void shouldNotRejectCancelledRequest() {

        accessRequest.setStatus(AccessRequestStatus.CANCELLED);

        when(accessRequestRepository.findWithDetailsById(100L))
                .thenReturn(Optional.of(accessRequest));

        assertThatThrownBy(() ->
                accessRequestService.rejectRequest(
                        100L,
                        "approver@example.com",
                        new ReviewAccessRequest("Rejected")
                )
        ).isInstanceOf(InvalidAccessRequestStateException.class);

        verifyNoInteractions(auditService);
    }

    @Test
    void shouldNotCancelAnotherUsersRequest() {

        when(accessRequestRepository.findWithDetailsById(100L))
                .thenReturn(Optional.of(accessRequest));

        assertThatThrownBy(() ->
                accessRequestService.cancelRequest(
                        100L,
                        "another-user@example.com"
                )
        ).isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(auditService);
        verify(accessRequestRepository, never()).flush();
    }

    @Test
    void shouldNotAllowSelfApproval() {

        when(accessRequestRepository.findWithDetailsById(100L))
                .thenReturn(Optional.of(accessRequest));

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(requester));

        assertThatThrownBy(() ->
                accessRequestService.approveRequest(
                        100L,
                        "user@example.com",
                        new ReviewAccessRequest("Approve myself")
                )
        ).isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(auditService);
        verify(accessRequestRepository, never()).flush();
    }
}

