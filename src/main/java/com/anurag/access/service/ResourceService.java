package com.anurag.access.service;

import com.anurag.access.dto.resource.CreateResourceRequest;
import com.anurag.access.dto.resource.ResourceResponse;
import com.anurag.access.entity.Resource;
import com.anurag.access.exception.DuplicateResourceException;
import com.anurag.access.exception.ResourceNotFoundException;
import com.anurag.access.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Transactional
    public ResourceResponse createResource(
            CreateResourceRequest request) {

        if (resourceRepository
                .existsByNameIgnoreCase(request.getName())) {

            throw new DuplicateResourceException(
                    "Resource with this name already exists"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Resource resource = new Resource();
        resource.setName(request.getName().trim());
        resource.setDescription(request.getDescription());
        resource.setResourceType(request.getResourceType());
        resource.setActive(true);
        resource.setCreatedAt(now);
        resource.setUpdatedAt(now);

        Resource saved = resourceRepository.save(resource);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> getActiveResources() {

        return resourceRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResourceResponse getResource(Long id) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: " + id
                        )
                );

        return mapToResponse(resource);
    }

    private ResourceResponse mapToResponse(Resource resource) {

        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getResourceType(),
                resource.isActive(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}
