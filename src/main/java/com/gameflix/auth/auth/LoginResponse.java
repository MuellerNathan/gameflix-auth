package com.gameflix.auth.auth;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds) {
}
