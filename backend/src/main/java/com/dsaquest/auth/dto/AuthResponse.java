package com.dsaquest.auth.dto;

import java.time.Instant;

public record AuthResponse(String accessToken, String tokenType, Instant accessTokenExpiresAt, UserResponse user) {
}
