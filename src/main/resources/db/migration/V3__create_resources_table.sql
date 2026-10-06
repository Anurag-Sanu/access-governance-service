CREATE TABLE resources (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(150) NOT NULL UNIQUE,
                           description VARCHAR(500),
                           resource_type VARCHAR(50) NOT NULL,
                           active BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_resources_active
    ON resources(active);