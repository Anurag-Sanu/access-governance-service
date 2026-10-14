-- Pagination for a requester's access request history.
CREATE INDEX IF NOT EXISTS idx_access_requests_requester_created
    ON access_requests (requester_id, created_at DESC, id DESC);

-- Pagination for requests filtered by requester and status.
CREATE INDEX IF NOT EXISTS idx_access_requests_requester_status_created
    ON access_requests (requester_id, status, created_at DESC, id DESC);

-- Pending approval queue, ordered by creation time.
CREATE INDEX IF NOT EXISTS idx_access_requests_status_created
    ON access_requests (status, created_at ASC, id ASC);
