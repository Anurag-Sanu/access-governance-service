package com.anurag.access.dto.resource;

import com.anurag.access.entity.ResourceType;

import java.time.LocalDateTime;

public record ResourceResponse(
        Long id,
        String name,
        String description,
        ResourceType resourceType,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
