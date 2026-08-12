package com.example.gymlog.domain.usecase

import com.example.gymlog.RoutineRepository
import com.example.gymlog.model.RoutineEntity
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import java.util.Calendar

class GetDashboardStatsUseCase @Inject constructor(
    private val repository: RoutineRepository
) {
    data class DashboardData(
        val userName: String,
        val greeting: String,
        val todayWorkout: TodayWorkout?,
        val stats: Stats,
        val allRoutinesCompleted: Boolean = false
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
        
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val completedToday = repository.getCompletedRoutineIdsSince(profileId, startOfToday)
        val routines = repository.getAllRoutines(profileId).firstOrNull() ?: emptyList()
        
        // Find the first routine not completed today
        val routineToSuggest = routines.find { it.id !in completedToday }
        val allCompleted = routines.isNotEmpty() && routines.all { it.id in completedToday }
        
        val todayWorkout = routineToSuggest?.let { routine ->
            val exercises = repository.getExercisesForRoutineSync(routine.id)
            TodayWorkout(
                routineId = routine.id,
                routineName = routine.name,
                exerciseCount = exercises.size,
                durationMinutes = exercises.size * 10
            )
        }

        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val volume = repository.getTotalVolumeSince(profileId, oneWeekAgo) ?: 0f
        val count = repository.getWorkoutCountSince(profileId, oneWeekAgo)
        val fav = repository.getFavoriteExercise(profileId) ?: "None"

        return DashboardData(
            userName = userName,
            greeting = greeting,
            todayWorkout = todayWorkout,
            stats = Stats(volume, count, fav),
            allRoutinesCompleted = allCompleted
        )
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
}
