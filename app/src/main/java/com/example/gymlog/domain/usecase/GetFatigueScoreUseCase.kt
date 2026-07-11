package com.example.gymlog.domain.usecase

import com.example.gymlog.RoutineRepository
import javax.inject.Inject

class GetFatigueScoreUseCase @Inject constructor(
    private val repository: RoutineRepository
) {
    suspend operator fun invoke(profileId: Int): Int {
        val now = System.currentTimeMillis()
        val oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000L)
        val twoWeeksAgo = now - (14 * 24 * 60 * 60 * 1000L)

        // Track: Weekly Volume, Density (not tracked yet but volume is proxy), Frequency
        val currentWeekVolume = repository.getTotalVolumeSince(profileId, oneWeekAgo) ?: 0f
        val previousWeekVolume = (repository.getTotalVolumeSince(profileId, twoWeeksAgo) ?: 0f) - currentWeekVolume

        val currentWeekCount = repository.getWorkoutCountSince(profileId, oneWeekAgo)

        // Fatigue increases if current week volume > previous week by significant margin
        // or if training frequency is very high (e.g. > 5 days)
        var fatigue = 30 // Base metabolic fatigue
        
        if (previousWeekVolume > 0) {
            val volumeIncreaseRatio = currentWeekVolume / previousWeekVolume
            if (volumeIncreaseRatio > 1.2) fatigue += 20
            if (volumeIncreaseRatio > 1.5) fatigue += 30
        }

        if (currentWeekCount >= 5) fatigue += 20
        if (currentWeekCount >= 6) fatigue += 40

        return fatigue.coerceIn(0, 100)
    }
}
