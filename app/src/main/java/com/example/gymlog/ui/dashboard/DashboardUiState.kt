package com.example.gymlog.ui.dashboard

import com.example.gymlog.model.WorkoutSessionEntity

data class DashboardUiState(
    val greeting: String = "",
    val userName: String = "",
    val streak: Int = 0,
    val todayPlan: TodayPlan? = null,
    val recoveryScore: Int = 0,
    val weeklyGoal: WeeklyGoal = WeeklyGoal(0, 5),
    val recentWorkout: WorkoutSessionEntity? = null,
    val coachInsight: String = "",
    val isLoading: Boolean = false
)

data class TodayPlan(
    val routineId: Int,
    val routineName: String,
    val exerciseCount: Int,
    val estimatedMinutes: Int
)

data class WeeklyGoal(
    val completed: Int,
    val total: Int
)
