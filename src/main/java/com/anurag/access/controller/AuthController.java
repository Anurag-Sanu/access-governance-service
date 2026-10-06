package com.anurag.access.controller;

import com.anurag.access.dto.auth.LoginRequest;
import com.anurag.access.dto.auth.LoginResponse;
import com.anurag.access.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {

        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> currentUser(
            Authentication authentication) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("email", authentication.getName());

        response.put(
                "authorities",
                authentication.getAuthorities()
        );

        return ResponseEntity.ok(response);
    }
}
