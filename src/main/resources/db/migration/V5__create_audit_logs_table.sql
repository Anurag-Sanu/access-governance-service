CREATE TABLE audit_logs (
                            id BIGSERIAL PRIMARY KEY,

                            access_request_id BIGINT NOT NULL,
                            actor_id BIGINT NOT NULL,

                            action VARCHAR(30) NOT NULL,
                            previous_status VARCHAR(30) NOT NULL,
                            new_status VARCHAR(30) NOT NULL,

                            comment VARCHAR(1000),

                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_audit_access_request
                                FOREIGN KEY (access_request_id)
                                    REFERENCES access_requests(id),

                            CONSTRAINT fk_audit_actor
                                FOREIGN KEY (actor_id)
                                    REFERENCES users(id),

                            CONSTRAINT chk_audit_action
                                CHECK (action IN ('APPROVED', 'REJECTED', 'CANCELLED')),

    CONSTRAINT chk_audit_previous_status
        CHECK (previous_status IN (
            'PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'
        )),

    CONSTRAINT chk_audit_new_status
        CHECK (new_status IN (
            'PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'
        ))
);

CREATE INDEX idx_audit_logs_request_created
    ON audit_logs(access_request_id, created_at, id);