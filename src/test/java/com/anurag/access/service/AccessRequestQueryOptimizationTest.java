package com.anurag.access.service;

import com.anurag.access.dto.accessrequest.AccessRequestResponse;
import com.anurag.access.dto.common.PagedResponse;
import com.anurag.access.repository.AccessRequestRepository;
import com.anurag.access.support.AccessRequestTestFixture;
import com.anurag.access.support.AccessRequestTestFixture.Fixture;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(AccessRequestTestFixture.class)
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
class AccessRequestQueryOptimizationTest {

    @Autowired
    private AccessRequestService accessRequestService;

    @Autowired
    private AccessRequestTestFixture fixtureHelper;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private final List<Fixture> fixtures = new ArrayList<>();

    @AfterEach
    void cleanUp() {

        for (Fixture fixture : fixtures) {
            fixtureHelper.cleanup(fixture);
        }

        fixtures.clear();
    }

    @Test
    void shouldFetchPendingRequestsWithoutNPlusOneQueries() {

        // Create multiple access requests.
        for (int i = 0; i < 5; i++) {
            fixtures.add(fixtureHelper.create());
        }

        SessionFactory sessionFactory =
                entityManagerFactory.unwrap(SessionFactory.class);

        Statistics statistics = sessionFactory.getStatistics();

        statistics.setStatisticsEnabled(true);
        statistics.clear();

        PagedResponse<AccessRequestResponse> response =
                accessRequestService.getPendingRequestsPaged(
                        0,
                        10,
                        "asc"
                );

        long statementCount =
                statistics.getPrepareStatementCount();

        assertThat(response.content()).isNotEmpty();

        // One page query, plus potentially one count query.
        assertThat(statementCount).isLessThanOrEqualTo(2);
    }
}

