package com.anurag.access.dto.accessrequest;

import jakarta.validation.constraints.Size;

public record ReviewAccessRequest(
        @Size(max = 1000)
        String comment
) {
}
