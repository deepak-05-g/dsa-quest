package com.dsaquest.dto;

import java.time.Instant;

public record HealthResponse(String application, String status, Instant timestamp) {
}
