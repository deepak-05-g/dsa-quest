package com.dsaquest.auth.repository;

import com.dsaquest.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken token set token.revokedAt = :revokedAt where token.familyId = :familyId and token.revokedAt is null")
    int revokeFamily(UUID familyId, Instant revokedAt);
}
