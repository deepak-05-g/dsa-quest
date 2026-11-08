package com.dsaquest.dashboard.dto;

import com.dsaquest.auth.dto.UserResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(
        UserResponse user,
        XpSummary xp,
        StreakSummary streak,
        LearningSummary learning,
        ContinueLearning continueLearning,
        List<Achievement> achievements,
        List<ActivityItem> recentActivity) {

    public record XpSummary(int totalXp, int level, int xpForNextLevel, int xpProgress) {
    }

    public record StreakSummary(int currentDays, int longestDays, boolean activeToday) {
    }

    public record LearningSummary(int completedLessons, int publishedLessons, int completionPercent) {
    }

    public record ContinueLearning(String lessonTitle, String topic, Instant lastAccessedAt) {
    }

    public record Achievement(String title, String description, Instant earnedAt) {
    }

    public record ActivityItem(String type, String description, int xpEarned, Instant occurredAt) {
    }
}
