package com.dsaquest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(appliedMigrationCount).isGreaterThanOrEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name IN ('users', 'refresh_tokens')",
                Integer.class)).isEqualTo(2);
    }
}
