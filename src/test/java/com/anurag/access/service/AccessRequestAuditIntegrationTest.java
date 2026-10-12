package com.anurag.access.service;

import com.anurag.access.dto.accessrequest.ReviewAccessRequest;
import com.anurag.access.entity.*;
import com.anurag.access.repository.*;
import com.anurag.access.support.AccessRequestTestFixture;
import com.anurag.access.support.AccessRequestTestFixture.Fixture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(AccessRequestTestFixture.class)
class AccessRequestAuditIntegrationTest {

    @Autowired
    private AccessRequestService accessRequestService;

    @Autowired
    private AccessRequestRepository accessRequestRepository;

    @Autowired
    private AccessRequestTestFixture fixtureHelper;

    @MockitoBean
    private AuditLogRepository auditLogRepository;

    private Fixture fixture;

    @AfterEach
    void cleanUp() {
        fixtureHelper.cleanup(fixture);
    }

    @Test
    void shouldRollbackApprovalWhenAuditInsertFails() {

        fixture = fixtureHelper.create();

        doThrow(new RuntimeException("Simulated audit failure"))
                .when(auditLogRepository)
                .save(any(AuditLog.class));

        assertThatThrownBy(() ->
                accessRequestService.approveRequest(
                        fixture.requestId(),
                        fixture.reviewerEmail(),
                        new ReviewAccessRequest("Approved")
                )
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Simulated audit failure");

        AccessRequest savedRequest = accessRequestRepository
                .findById(fixture.requestId())
                .orElseThrow();

        assertThat(savedRequest.getStatus())
                .isEqualTo(AccessRequestStatus.PENDING);

        assertThat(savedRequest.getReviewedBy())
                .isNull();

        verify(auditLogRepository).save(any(AuditLog.class));
    }
}


