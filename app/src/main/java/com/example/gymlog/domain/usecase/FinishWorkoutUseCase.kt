package com.example.gymlog.domain.usecase

import com.example.gymlog.RoutineRepository
import com.example.gymlog.SessionRepository
import com.example.gymlog.health.HealthConnectManager
import com.example.gymlog.model.WorkoutStatus
import java.time.Instant
import javax.inject.Inject

class FinishWorkoutUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val routineRepository: RoutineRepository,
    private val healthConnectManager: HealthConnectManager
) {
    suspend operator fun invoke(sessionId: Int, notes: String? = null): String? {
        val session = sessionRepository.getSessionById(sessionId) ?: return null
        val sets = sessionRepository.getSetsForSessionSync(sessionId)
        
        val endTime = System.currentTimeMillis()
        val durationMillis = endTime - session.startTime
        val totalVolume = sets.filter { it.isCompleted }.sumOf { it.weight * it.reps }
        
        val updatedSession = session.copy(
            endTime = endTime,
            status = WorkoutStatus.COMPLETED,
            notes = notes,
            durationMillis = durationMillis,
            totalVolume = totalVolume
        )
        sessionRepository.updateSession(updatedSession)

        // PR Detection
        val prsDetected = mutableListOf<String>()
        sets.filter { it.isCompleted }.groupBy { it.exerciseName }.forEach { (exerciseName, exerciseSets) ->
            val maxWeightThisSession = exerciseSets.maxOf { it.weight }
            val maxRepsAtMaxWeight = exerciseSets.filter { it.weight == maxWeightThisSession }.maxOf { it.reps }
            
            val prevMaxWeight = routineRepository.getMaxWeightForExercise(session.profileId, exerciseName)
            val prevMaxReps = routineRepository.getMaxRepsForExercise(session.profileId, exerciseName)

            if (prevMaxWeight == null || maxWeightThisSession > prevMaxWeight.toDouble()) {
                prsDetected.add("New Max Weight for $exerciseName: ${maxWeightThisSession}kg!")
            } else if (prevMaxReps == null || (maxWeightThisSession == prevMaxWeight.toDouble() && maxRepsAtMaxWeight > prevMaxReps)) {
                prsDetected.add("New Rep Record for $exerciseName: $maxRepsAtMaxWeight reps at ${maxWeightThisSession}kg!")
            }
        }

        // Health Connect Sync
        if (healthConnectManager.isHealthConnectAvailable() && healthConnectManager.hasAllPermissions()) {
            val routine = routineRepository.getRoutineById(session.routineId)
            healthConnectManager.writeWorkoutSession(
                startTime = Instant.ofEpochMilli(session.startTime),
                endTime = Instant.ofEpochMilli(endTime),
                title = routine?.name ?: "Gym Workout",
                notes = notes
            )
        }

        return if (prsDetected.isNotEmpty()) prsDetected.joinToString("\n") else null
    }
}
