package com.anurag.access.repository;

import com.anurag.access.entity.AccessRequest;
import com.anurag.access.entity.AccessRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

import java.util.List;

public interface AccessRequestRepository
        extends JpaRepository<AccessRequest, Long> {

    List<AccessRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    List<AccessRequest> findByStatusOrderByCreatedAtAsc(AccessRequestStatus status);

    boolean existsByRequesterIdAndResourceIdAndStatus(
            Long requesterId,
            Long resourceId,
            AccessRequestStatus status
    );

    @EntityGraph(attributePaths = {"requester", "resource", "reviewedBy"})
    Optional<AccessRequest> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"requester", "resource", "reviewedBy"})
    Page<AccessRequest> findByRequesterId(
            Long requesterId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"requester", "resource", "reviewedBy"})
    Page<AccessRequest> findByRequesterIdAndStatus(
            Long requesterId,
            AccessRequestStatus status,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"requester", "resource", "reviewedBy"})
    Page<AccessRequest> findByStatus(
            AccessRequestStatus status,
            Pageable pageable
    );
}
