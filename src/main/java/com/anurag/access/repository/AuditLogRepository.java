package com.anurag.access.repository;

import com.anurag.access.entity.AuditLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    @EntityGraph(attributePaths = {"actor"})
    List<AuditLog> findByAccessRequestIdOrderByCreatedAtAscIdAsc(
            Long accessRequestId
    );
}
