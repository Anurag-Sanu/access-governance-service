CREATE TABLE access_requests (
                                 id BIGSERIAL PRIMARY KEY,

                                 requester_id BIGINT NOT NULL,
                                 resource_id BIGINT NOT NULL,

                                 status VARCHAR(50) NOT NULL,

                                 justification VARCHAR(1000) NOT NULL,

                                 reviewed_by BIGINT,
                                 review_comment VARCHAR(1000),

                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 reviewed_at TIMESTAMP,

                                 version BIGINT NOT NULL DEFAULT 0,

                                 CONSTRAINT fk_access_request_requester
                                     FOREIGN KEY (requester_id)
                                         REFERENCES users(id),

                                 CONSTRAINT fk_access_request_resource
                                     FOREIGN KEY (resource_id)
                                         REFERENCES resources(id),

                                 CONSTRAINT fk_access_request_reviewer
                                     FOREIGN KEY (reviewed_by)
                                         REFERENCES users(id)
);

CREATE INDEX idx_access_requests_requester
    ON access_requests(requester_id);

CREATE INDEX idx_access_requests_resource
    ON access_requests(resource_id);

CREATE INDEX idx_access_requests_status
    ON access_requests(status);

CREATE INDEX idx_access_requests_status_created
    ON access_requests(status, created_at);