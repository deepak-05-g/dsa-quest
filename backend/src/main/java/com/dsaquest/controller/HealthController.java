package com.dsaquest.controller;

import com.dsaquest.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check whether the DSA QUEST backend is running")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("dsa-quest", "UP", Instant.now()));
    }
}
