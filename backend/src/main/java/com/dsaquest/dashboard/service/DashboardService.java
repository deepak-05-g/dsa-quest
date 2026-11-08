package com.dsaquest.dashboard.service;

import com.dsaquest.auth.dto.UserResponse;
import com.dsaquest.auth.entity.User;
import com.dsaquest.dashboard.dto.DashboardResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    private static final ZoneOffset ACTIVITY_ZONE = ZoneOffset.UTC;
    private final JdbcTemplate jdbcTemplate;

    public DashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User user) {
        UUID userId = user.getId();
        int totalXp = queryInt("SELECT COALESCE(SUM(amount), 0) FROM xp_transactions WHERE user_id = ?", userId);
        int level = calculateLevel(totalXp);
        int nextLevelXp = 100 * level * level;
        int previousLevelXp = 100 * (level - 1) * (level - 1);
        int progress = totalXp - previousLevelXp;

        List<LocalDate> activeDates = jdbcTemplate.query(
                "SELECT DISTINCT activity_date FROM daily_activity WHERE user_id = ? ORDER BY activity_date DESC",
                (resultSet, rowNum) -> resultSet.getDate(1).toLocalDate(), userId);
        Streak streak = calculateStreak(activeDates, LocalDate.now(ACTIVITY_ZONE));

        int publishedLessons = queryInt("SELECT COUNT(*) FROM lessons WHERE published = TRUE");
        int completedLessons = queryInt("""
                SELECT COUNT(*) FROM learning_progress progress
                JOIN lessons lesson ON lesson.id = progress.lesson_id
                WHERE progress.user_id = ? AND progress.completed = TRUE AND lesson.published = TRUE
                """, userId);
        int completionPercent = publishedLessons == 0 ? 0 : completedLessons * 100 / publishedLessons;

        DashboardResponse.ContinueLearning continueLearning = jdbcTemplate.query("""
                SELECT lesson.title, lesson.topic, progress.last_accessed_at
                FROM learning_progress progress
                JOIN lessons lesson ON lesson.id = progress.lesson_id
                WHERE progress.user_id = ? AND progress.completed = FALSE
                  AND progress.last_accessed_at IS NOT NULL
                ORDER BY progress.last_accessed_at DESC
                LIMIT 1
                """, (resultSet, rowNum) -> new DashboardResponse.ContinueLearning(
                        resultSet.getString("title"),
                        resultSet.getString("topic"),
                        resultSet.getTimestamp("last_accessed_at").toInstant()), userId)
                .stream().findFirst().orElse(null);

        List<DashboardResponse.Achievement> achievements = jdbcTemplate.query("""
                SELECT achievement.title, achievement.description, earned.earned_at
                FROM user_achievements earned
                JOIN achievements achievement ON achievement.id = earned.achievement_id
                WHERE earned.user_id = ?
                ORDER BY earned.earned_at DESC
                """, (resultSet, rowNum) -> new DashboardResponse.Achievement(
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getTimestamp("earned_at").toInstant()), userId);

        List<DashboardResponse.ActivityItem> recentActivity = jdbcTemplate.query("""
                SELECT source, amount, created_at
                FROM xp_transactions
                WHERE user_id = ?
                ORDER BY created_at DESC
                LIMIT 10
                """, (resultSet, rowNum) -> new DashboardResponse.ActivityItem(
                        "XP_EARNED",
                        resultSet.getString("source"),
                        resultSet.getInt("amount"),
                        resultSet.getTimestamp("created_at").toInstant()), userId);

        return new DashboardResponse(
                UserResponse.from(user),
                new DashboardResponse.XpSummary(totalXp, level, nextLevelXp, Math.max(progress, 0)),
                new DashboardResponse.StreakSummary(streak.currentDays(), streak.longestDays(),
                        activeDates.contains(LocalDate.now(ACTIVITY_ZONE))),
                new DashboardResponse.LearningSummary(completedLessons, publishedLessons, completionPercent),
                continueLearning,
                achievements,
                recentActivity);
    }

    static int calculateLevel(int totalXp) {
        int level = 1;
        while (100 * level * level <= totalXp) {
            level++;
        }
        return level;
    }

    static Streak calculateStreak(List<LocalDate> dates, LocalDate today) {
        if (dates.isEmpty()) {
            return new Streak(0, 0);
        }
        int longest = 1;
        int running = 1;
        for (int index = 1; index < dates.size(); index++) {
            if (dates.get(index - 1).minusDays(1).equals(dates.get(index))) {
                running++;
                longest = Math.max(longest, running);
            } else {
                running = 1;
            }
        }
        int current = 0;
        LocalDate expected = today;
        for (LocalDate date : dates) {
            if (!date.equals(expected)) {
                if (current == 0 && date.equals(today.minusDays(1))) {
                    expected = date;
                } else {
                    break;
                }
            }
            current++;
            expected = date.minusDays(1);
        }
        return new Streak(current, longest);
    }

    private int queryInt(String sql, Object... args) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }

    private record Streak(int currentDays, int longestDays) {
    }
}
