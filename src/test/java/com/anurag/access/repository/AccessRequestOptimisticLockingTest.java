package com.anurag.access.repository;

import com.anurag.access.entity.AccessRequest;
import com.anurag.access.entity.AccessRequestStatus;
import com.anurag.access.support.AccessRequestTestFixture;
import com.anurag.access.support.AccessRequestTestFixture.Fixture;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(AccessRequestTestFixture.class)
class AccessRequestOptimisticLockingTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private AccessRequestTestFixture fixtureHelper;

    private Fixture fixture;

    @AfterEach
    void cleanUp() {
        fixtureHelper.cleanup(fixture);
    }

    @Test
    void shouldRejectStaleConcurrentUpdate() {

        fixture = fixtureHelper.create();

        Long requestId = fixture.requestId();

        AccessRequest snapshotA = loadDetached(requestId);
        AccessRequest snapshotB = loadDetached(requestId);

        assertThat(snapshotA.getVersion())
                .isEqualTo(snapshotB.getVersion());

        assertThat(snapshotA.getStatus())
                .isEqualTo(AccessRequestStatus.PENDING);

        TransactionTemplate transaction =
                new TransactionTemplate(transactionManager);

        snapshotA.setStatus(AccessRequestStatus.APPROVED);

        transaction.executeWithoutResult(status -> {
            entityManager.merge(snapshotA);
            entityManager.flush();
        });

        snapshotB.setStatus(AccessRequestStatus.REJECTED);

        assertThatThrownBy(() ->
                transaction.executeWithoutResult(status -> {
                    entityManager.merge(snapshotB);
                    entityManager.flush();
                })
        ).isInstanceOfAny(
                ObjectOptimisticLockingFailureException.class,
                OptimisticLockException.class
        );

        AccessRequest finalRequest = loadDetached(requestId);

        assertThat(finalRequest.getStatus())
                .isEqualTo(AccessRequestStatus.APPROVED);

        assertThat(finalRequest.getVersion())
                .isGreaterThan(snapshotB.getVersion());
    }

    private AccessRequest loadDetached(Long id) {

        TransactionTemplate transaction =
                new TransactionTemplate(transactionManager);

        return transaction.execute(status -> {

            AccessRequest request =
                    entityManager.find(AccessRequest.class, id);

            assertThat(request).isNotNull();

            entityManager.detach(request);

            return request;
        });
    }
}



