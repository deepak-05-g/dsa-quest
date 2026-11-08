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
}
