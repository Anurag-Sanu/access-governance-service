package com.anurag.access.controller;

import com.anurag.access.dto.resource.CreateResourceRequest;
import com.anurag.access.dto.resource.ResourceResponse;
import com.anurag.access.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceResponse> createResource(
            @Valid @RequestBody CreateResourceRequest request) {

        ResourceResponse response =
                resourceService.createResource(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ResourceResponse>>
    getActiveResources() {

        return ResponseEntity.ok(
                resourceService.getActiveResources()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResource(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                resourceService.getResource(id)
        );
    }
}
