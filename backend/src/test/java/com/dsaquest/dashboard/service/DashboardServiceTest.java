package com.dsaquest.dashboard.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardServiceTest {

    @Test
    void calculatesLevelFromCentralizedXpThresholds() {
        assertThat(DashboardService.calculateLevel(0)).isEqualTo(1);
        assertThat(DashboardService.calculateLevel(99)).isEqualTo(1);
        assertThat(DashboardService.calculateLevel(100)).isEqualTo(2);
        assertThat(DashboardService.calculateLevel(399)).isEqualTo(2);
        assertThat(DashboardService.calculateLevel(400)).isEqualTo(3);
    }

    @Test
    void returnsZeroCurrentAndLongestStreakForNoActivity() {
        assertThat(DashboardService.calculateStreak(List.of(), LocalDate.of(2026, 1, 1)))
                .extracting("currentDays", "longestDays")
                .containsExactly(0, 0);
    }

    @Test
    void calculatesCurrentAndLongestStreaksAcrossGaps() {
        LocalDate today = LocalDate.of(2026, 1, 10);
        assertThat(DashboardService.calculateStreak(
                List.of(today, today.minusDays(1), today.minusDays(2), today.minusDays(4)),
                today))
                .extracting("currentDays", "longestDays")
                .containsExactly(3, 3);
        assertThat(DashboardService.calculateStreak(
                List.of(today.minusDays(1), today.minusDays(2), today.minusDays(4), today.minusDays(5)),
                today))
                .extracting("currentDays", "longestDays")
                .containsExactly(2, 2);
    }

    @Test
    void doesNotCountAStaleRunAsCurrent() {
        LocalDate today = LocalDate.of(2026, 1, 10);
        assertThat(DashboardService.calculateStreak(
                List.of(today.minusDays(3), today.minusDays(4)),
                today))
                .extracting("currentDays", "longestDays")
                .containsExactly(0, 2);
    }
}
