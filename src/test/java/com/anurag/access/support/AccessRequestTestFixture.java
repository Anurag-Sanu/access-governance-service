
package com.anurag.access.support;

import com.anurag.access.entity.*;
import com.anurag.access.repository.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AccessRequestTestFixture {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public AccessRequestTestFixture(
            UserRepository userRepository,
            ResourceRepository resourceRepository,
            AccessRequestRepository accessRequestRepository,
            JdbcTemplate jdbcTemplate,
            PlatformTransactionManager transactionManager) {

        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.accessRequestRepository = accessRequestRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate =
                new TransactionTemplate(transactionManager);
    }

    public Fixture create() {

        return transactionTemplate.execute(status -> {

            String suffix = UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();

            User requester = new User();
            requester.setEmail("fixture-user-" + suffix + "@example.com");
            requester.setPasswordHash("encoded-password");
            requester.setRole(Role.USER);
            requester.setCreatedAt(now);
            requester.setUpdatedAt(now);
            requester = userRepository.saveAndFlush(requester);

            User reviewer = new User();
            reviewer.setEmail("fixture-reviewer-" + suffix + "@example.com");
            reviewer.setPasswordHash("encoded-password");
            reviewer.setRole(Role.APPROVER);
            reviewer.setCreatedAt(now);
            reviewer.setUpdatedAt(now);
            reviewer = userRepository.saveAndFlush(reviewer);

            Resource resource = new Resource();
            resource.setName("Fixture Resource " + suffix);
            resource.setDescription("Integration test resource");
            resource.setResourceType(ResourceType.DATABASE);
            resource.setActive(true);
            resource.setCreatedAt(now);
            resource.setUpdatedAt(now);
            resource = resourceRepository.saveAndFlush(resource);

            AccessRequest request = new AccessRequest();
            request.setRequester(requester);
            request.setResource(resource);
            request.setStatus(AccessRequestStatus.PENDING);
            request.setJustification("Integration testing");
            request.setCreatedAt(now);
            request.setUpdatedAt(now);
            request = accessRequestRepository.saveAndFlush(request);

            return new Fixture(
                    request.getId(),
                    requester.getId(),
                    reviewer.getId(),
                    resource.getId(),
                    requester.getEmail(),
                    reviewer.getEmail()
            );
        });
    }

    public void cleanup(Fixture fixture) {

        if (fixture == null) {
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {

            jdbcTemplate.update(
                    "DELETE FROM audit_logs WHERE access_request_id = ?",
                    fixture.requestId()
            );

            jdbcTemplate.update(
                    "DELETE FROM access_requests WHERE id = ?",
                    fixture.requestId()
            );

            jdbcTemplate.update(
                    "DELETE FROM resources WHERE id = ?",
                    fixture.resourceId()
            );

            jdbcTemplate.update(
                    "DELETE FROM users WHERE id IN (?, ?)",
                    fixture.requesterId(),
                    fixture.reviewerId()
            );
        });
    }

    public record Fixture(
            Long requestId,
            Long requesterId,
            Long reviewerId,
            Long resourceId,
            String requesterEmail,
            String reviewerEmail
    ) {
    }
}

