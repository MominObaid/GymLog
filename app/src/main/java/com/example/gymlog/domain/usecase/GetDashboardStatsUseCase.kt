package com.example.gymlog.domain.usecase

import com.example.gymlog.RoutineRepository
import com.example.gymlog.model.RoutineEntity
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor(
    private val repository: RoutineRepository
) {
    data class DashboardData(
        val userName: String,
        val greeting: String,
        val todayWorkout: TodayWorkout?,
        val stats: Stats
    )

    data class TodayWorkout(
        val routineId: Int,
        val routineName: String,
        val exerciseCount: Int,
        val durationMinutes: Int
    )

    data class Stats(
        val weeklyVolume: Float,
        val workoutCount: Int,
        val favoriteExercise: String
    )

    suspend operator fun invoke(profileId: Int, userName: String): DashboardData {
        val greeting = getGreeting()
        
        val routines = repository.getAllRoutines(profileId).firstOrNull()
        val todayWorkout = if (!routines.isNullOrEmpty()) {
            val routine = routines.first()
            TodayWorkout(routine.id, routine.name, 0, 45) // Simplification for now
        } else {
            null
        }

        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val volume = repository.getTotalVolumeSince(profileId, oneWeekAgo) ?: 0f
        val count = repository.getWorkoutCountSince(profileId, oneWeekAgo)
        val fav = repository.getFavoriteExercise(profileId) ?: "None"

        return DashboardData(
            userName = userName,
            greeting = greeting,
            todayWorkout = todayWorkout,
            stats = Stats(volume, count, fav)
        )
    }

    private fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
}
