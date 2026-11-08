package com.dsaquest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.sql.Timestamp;
import java.util.UUID;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DSA_QUEST_DB_INTEGRATION", matches = "true")
class DsaQuestApplicationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void connectsAndRunsFlywayMigration() {
        Integer migrationTableCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'flyway_schema_history'
                """,
                Integer.class);
        Integer appliedMigrationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history",
                Integer.class);

        assertThat(migrationTableCount).isEqualTo(1);
        assertThat(appliedMigrationCount).isGreaterThanOrEqualTo(5);
        assertThat(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name IN ('users', 'refresh_tokens')",
                Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name IN ('xp_transactions', 'daily_activity', 'lessons', 'learning_progress', 'achievements', 'user_achievements')",
                Integer.class)).isEqualTo(6);
    }

    @Test
    void duplicateRewardReferenceIsRejectedByDatabase() {
        UUID userId = UUID.randomUUID();
        UUID rewardId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO users (id, full_name, email, password_hash, role, enabled, created_at, updated_at) VALUES (?, ?, ?, ?, 'STUDENT', true, ?, ?)",
                userId, "XP Constraint Test", "xp-constraint-" + userId + "@example.test", "hash", Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));
        jdbcTemplate.update(
                "INSERT INTO xp_transactions (id, user_id, amount, source, reference_id, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), userId, 25, "LESSON", rewardId.toString(), Timestamp.from(Instant.now()));

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO xp_transactions (id, user_id, amount, source, reference_id, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), userId, 25, "LESSON", rewardId.toString(), Timestamp.from(Instant.now())))
                .isInstanceOf(DuplicateKeyException.class);
    }
}
