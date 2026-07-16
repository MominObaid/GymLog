package com.example.gymlog.ui.dashboard

import com.example.gymlog.RoutineRepository
import com.example.gymlog.health.HealthConnectManager
import com.example.gymlog.model.WorkoutSessionEntity
import com.example.gymlog.utils.StreakCalculator
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val healthConnectManager: HealthConnectManager
) {
    suspend fun getProfile() = routineRepository.getProfile()

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    suspend fun getStreak(profileId: Int): Int {
        val times = routineRepository.getAllSessionTimes(profileId)
        return StreakCalculator.calculateStreak(times)
    }

    suspend fun getTodayPlan(profileId: Int): TodayPlan? {
        val routines = routineRepository.getAllRoutines(profileId).firstOrNull()
        return routines?.firstOrNull()?.let { routine ->
            val exercises = routineRepository.getExercisesForRoutine(routine.id).firstOrNull() ?: emptyList()
            
            // Formula: Each exercise = 3 sets, Each set = 2 minutes, Rest = 90 sec (1.5 min)
            // Total per exercise = 3 * 2 + 2 * 1.5 = 6 + 3 = 9 minutes
            val estimatedMinutes = exercises.size * 9
            
            TodayPlan(
                routineId = routine.id,
                routineName = routine.name,
                exerciseCount = exercises.size,
                estimatedMinutes = estimatedMinutes
            )
        }
    }

    suspend fun getRecentWorkout(profileId: Int): WorkoutSessionEntity? {
        return routineRepository.getRecentSessions(profileId).firstOrNull()?.firstOrNull()
    }

    suspend fun getWeeklyGoalStatus(profileId: Int): WeeklyGoal {
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val completed = routineRepository.getWorkoutCountSince(profileId, oneWeekAgo)
        return WeeklyGoal(completed, 5) // Default goal of 5
    }

    suspend fun getSleepScore(): Int {
        if (!healthConnectManager.isHealthConnectAvailable()) return 0
        if (!healthConnectManager.hasAllPermissions()) return 0
        
        val start = Instant.now().minus(1, ChronoUnit.DAYS)
        val end = Instant.now()
        val sleepHours = healthConnectManager.readSleepDuration(start, end)
        
        // Simple score: 8 hours = 100%
        return ((sleepHours / 8.0) * 100).toInt().coerceAtMost(100)
    }
}
