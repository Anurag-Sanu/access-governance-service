package com.anurag.access.service;

import com.anurag.access.dto.accessrequest.AccessRequestResponse;
import com.anurag.access.dto.common.PagedResponse;
import com.anurag.access.entity.*;
import com.anurag.access.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessRequestPaginationServiceTest {

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
    private Resource resource;
    private AccessRequest accessRequest;

    @BeforeEach
    void setUp() {

        requester = new User();
        requester.setEmail("user@example.com");
        requester.setRole(Role.USER);

        resource = new Resource();
        resource.setName("Customer Database");
        resource.setResourceType(ResourceType.DATABASE);
        resource.setActive(true);

        accessRequest = new AccessRequest();
        accessRequest.setRequester(requester);
        accessRequest.setResource(resource);
        accessRequest.setStatus(AccessRequestStatus.PENDING);
        accessRequest.setJustification("Need database access");
        accessRequest.setCreatedAt(LocalDateTime.now());
        accessRequest.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void shouldReturnPaginatedMyRequests() {

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(requester));

        Page<AccessRequest> page = new PageImpl<>(
                List.of(accessRequest)
        );

        when(accessRequestRepository.findByRequesterId(
                eq(requester.getId()),
                any(Pageable.class)
        )).thenReturn(page);

        PagedResponse<AccessRequestResponse> result =
                accessRequestService.getMyRequestsPaged(
                        "user@example.com",
                        null,
                        0,
                        10,
                        "desc"
                );

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).status())
                .isEqualTo(AccessRequestStatus.PENDING);

        verify(accessRequestRepository).findByRequesterId(
                eq(requester.getId()),
                any(Pageable.class)
        );
    }

    @Test
    void shouldFilterMyRequestsByStatus() {

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(requester));

        Page<AccessRequest> page =
                new PageImpl<>(List.of(accessRequest));

        when(accessRequestRepository.findByRequesterIdAndStatus(
                eq(requester.getId()),
                eq(AccessRequestStatus.PENDING),
                any(Pageable.class)
        )).thenReturn(page);

        PagedResponse<AccessRequestResponse> result =
                accessRequestService.getMyRequestsPaged(
                        "user@example.com",
                        AccessRequestStatus.PENDING,
                        0,
                        10,
                        "desc"
                );

        assertThat(result.content()).hasSize(1);

        verify(accessRequestRepository)
                .findByRequesterIdAndStatus(
                        eq(requester.getId()),
                        eq(AccessRequestStatus.PENDING),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldReturnPaginatedPendingRequests() {

        Page<AccessRequest> page =
                new PageImpl<>(List.of(accessRequest));

        when(accessRequestRepository.findByStatus(
                eq(AccessRequestStatus.PENDING),
                any(Pageable.class)
        )).thenReturn(page);

        PagedResponse<AccessRequestResponse> result =
                accessRequestService.getPendingRequestsPaged(
                        0,
                        10,
                        "asc"
                );

        assertThat(result.content()).hasSize(1);

        verify(accessRequestRepository).findByStatus(
                eq(AccessRequestStatus.PENDING),
                any(Pageable.class)
        );
    }

    @Test
    void shouldRejectNegativePageNumber() {

        assertThatThrownBy(() ->
                accessRequestService.getPendingRequestsPaged(
                        -1,
                        10,
                        "asc"
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page number");
    }

    @Test
    void shouldRejectPageSizeGreaterThan100() {

        assertThatThrownBy(() ->
                accessRequestService.getPendingRequestsPaged(
                        0,
                        101,
                        "asc"
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page size");
    }

    @Test
    void shouldRejectInvalidSortDirection() {

        assertThatThrownBy(() ->
                accessRequestService.getPendingRequestsPaged(
                        0,
                        10,
                        "invalid"
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Direction");
    }
}

