package com.anurag.access.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "access_request_id", nullable = false)
    private AccessRequest accessRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false, length = 30)
    private AccessRequestStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private AccessRequestStatus newStatus;

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected AuditLog() {
    }

    public AuditLog(
            AccessRequest accessRequest,
            User actor,
            AuditAction action,
            AccessRequestStatus previousStatus,
            AccessRequestStatus newStatus,
            String comment,
            LocalDateTime createdAt) {

        this.accessRequest = accessRequest;
        this.actor = actor;
        this.action = action;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public AccessRequest getAccessRequest() {
        return accessRequest;
    }

    public User getActor() {
        return actor;
    }

    public AuditAction getAction() {
        return action;
    }

    public AccessRequestStatus getPreviousStatus() {
        return previousStatus;
    }

    public AccessRequestStatus getNewStatus() {
        return newStatus;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
