package com.anurag.access.service;

import com.anurag.access.dto.accessrequest.ReviewAccessRequest;
import com.anurag.access.entity.*;
import com.anurag.access.exception.InvalidAccessRequestStateException;
import com.anurag.access.repository.AccessRequestRepository;
import com.anurag.access.repository.AuditLogRepository;
import com.anurag.access.support.AccessRequestTestFixture;
import com.anurag.access.support.AccessRequestTestFixture.Fixture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(AccessRequestTestFixture.class)
class AccessRequestAuditSuccessIntegrationTest {

    @Autowired
    private AccessRequestService accessRequestService;

    @Autowired
    private AccessRequestRepository accessRequestRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AccessRequestTestFixture fixtureHelper;

    private Fixture fixture;

    @AfterEach
    void cleanUp() {
        fixtureHelper.cleanup(fixture);
    }

    @Test
    void shouldCreateExactlyOneAuditEntryForSuccessfulApproval() {

        fixture = fixtureHelper.create();

        accessRequestService.approveRequest(
                fixture.requestId(),
                fixture.reviewerEmail(),
                new ReviewAccessRequest("Approved for testing")
        );

        AccessRequest savedRequest = accessRequestRepository
                .findById(fixture.requestId())
                .orElseThrow();

        assertThat(savedRequest.getStatus())
                .isEqualTo(AccessRequestStatus.APPROVED);

        List<AuditLog> logs = auditLogRepository
                .findByAccessRequestIdOrderByCreatedAtAscIdAsc(
                        fixture.requestId()
                );

        assertThat(logs).hasSize(1);

        AuditLog audit = logs.get(0);

        assertThat(audit.getAction())
                .isEqualTo(AuditAction.APPROVED);

        assertThat(audit.getPreviousStatus())
                .isEqualTo(AccessRequestStatus.PENDING);

        assertThat(audit.getNewStatus())
                .isEqualTo(AccessRequestStatus.APPROVED);

        assertThat(audit.getActor().getEmail())
                .isEqualTo(fixture.reviewerEmail());

        assertThat(audit.getComment())
                .isEqualTo("Approved for testing");
    }

    @Test
    void shouldNotCreateAnotherAuditEntryForRepeatedApproval() {

        fixture = fixtureHelper.create();

        ReviewAccessRequest review =
                new ReviewAccessRequest("Approved");

        accessRequestService.approveRequest(
                fixture.requestId(),
                fixture.reviewerEmail(),
                review
        );

        assertThatThrownBy(() ->
                accessRequestService.approveRequest(
                        fixture.requestId(),
                        fixture.reviewerEmail(),
                        review
                )
        ).isInstanceOf(InvalidAccessRequestStateException.class);

        List<AuditLog> logs = auditLogRepository
                .findByAccessRequestIdOrderByCreatedAtAscIdAsc(
                        fixture.requestId()
                );

        assertThat(logs).hasSize(1);

        assertThat(logs.get(0).getAction())
                .isEqualTo(AuditAction.APPROVED);
    }
}


