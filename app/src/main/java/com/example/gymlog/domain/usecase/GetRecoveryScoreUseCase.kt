package com.example.gymlog.domain.usecase

import com.example.gymlog.health.HealthConnectManager
import com.example.gymlog.RoutineRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetRecoveryScoreUseCase @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val repository: RoutineRepository
) {
    suspend operator fun invoke(profileId: Int): Int {
        if (!healthConnectManager.isHealthConnectAvailable() || !healthConnectManager.hasAllPermissions()) {
            return 70 // Baseline if no health data
        }

        val now = Instant.now()
        val yesterday = now.minus(1, ChronoUnit.DAYS)
        
        // 1. Sleep (40% weight)
        val sleepHours = healthConnectManager.readSleepDuration(yesterday.minus(8, ChronoUnit.HOURS), now)
        val sleepScore = (sleepHours / 8.0 * 100).coerceAtMost(100.0)

        // 2. Steps (20% weight)
        val steps = healthConnectManager.readSteps(yesterday.truncatedTo(ChronoUnit.DAYS), now)
        val stepScore = (steps / 10000.0 * 100).coerceAtMost(100.0)

        // 3. Workout Intensity & Frequency (40% weight)
        // Simplification: higher volume in last 3 days reduces recovery score slightly
        val last3Days = now.minus(3, ChronoUnit.DAYS).toEpochMilli()
        val recentVolume = repository.getTotalVolumeSince(profileId, last3Days) ?: 0f
        val intensityScore = (100 - (recentVolume / 5000.0 * 20)).coerceIn(0.0, 100.0)

        val finalScore = (sleepScore * 0.4 + stepScore * 0.2 + intensityScore * 0.4).toInt()
        return finalScore.coerceIn(0, 100)
    }
}
