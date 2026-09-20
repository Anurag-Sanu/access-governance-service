package com.anurag.access.dto.auth;

public record LoginResponse(
        String token,
        String tokenType
) {
}
