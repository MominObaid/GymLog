package com.example.gymlog.domain.usecase

import com.example.gymlog.RoutineRepository
import com.example.gymlog.model.ExerciseStat
import com.example.gymlog.utils.StreakCalculator
import javax.inject.Inject

class UpdateMilestonesUseCase @Inject constructor(
    private val repository: RoutineRepository
) {
    data class MilestonesResult(
        val totalWorkouts: Int,
        val currentStreak: Int,
        val longestStreak: Int,
        val weeklyVolume: Float,
        val strongestExercises: List<ExerciseStat>,
        val plateaus: List<String>
    )

    suspend operator fun invoke(profileId: Int): MilestonesResult {
        val totalWorkouts = repository.getWorkoutCount(profileId)
        val times = repository.getAllSessionTimes(profileId)
        val currentStreak = StreakCalculator.calculateStreak(times)
        val longestStreak = StreakCalculator.calculateLongestStreak(times)

        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val weeklyVolume = repository.getTotalVolumeSince(profileId, oneWeekAgo) ?: 0f
        
        val strongestExercises = repository.getStrongestExercises(profileId)

        // Plateau Detection
        val plateauList = mutableListOf<String>()
        strongestExercises.forEach { stat ->
            val history = repository.getWeightHistoryForExercise(profileId, stat.exerciseName)
            if (history.size >= 4) {
                val recent = history.take(4)
                if (recent.distinct().size == 1) {
                    plateauList.add("${stat.exerciseName} has stalled at ${stat.maxWeight}kg")
                }
            }
        }

        return MilestonesResult(
            totalWorkouts = totalWorkouts,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            weeklyVolume = weeklyVolume,
            strongestExercises = strongestExercises,
            plateaus = plateauList
        )
    }
}
