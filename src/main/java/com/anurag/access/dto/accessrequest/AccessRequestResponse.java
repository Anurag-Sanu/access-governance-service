package com.anurag.access.dto.accessrequest;

import com.anurag.access.entity.AccessRequestStatus;
import java.time.LocalDateTime;

public record AccessRequestResponse(

        Long id,

        Long requesterId,
        String requesterEmail,

        Long resourceId,
        String resourceName,

        AccessRequestStatus status,

        String justification,

        Long reviewedById,
        String reviewedByEmail,

        String reviewComment,

        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime reviewedAt,

        Long version
) {
}
