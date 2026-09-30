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
        
        // Auto-complete any set that has valid weight or reps entered
        sets.forEach { set ->
            if (!set.isCompleted && (set.weight > 0 || set.reps > 0)) {
                val completedSet = set.copy(isCompleted = true)
                sessionRepository.updateSet(completedSet)
            }
        }

        // Fetch refreshed sets after auto-completion
        val updatedSets = sessionRepository.getSetsForSessionSync(sessionId)
        val completedSets = updatedSets.filter { it.isCompleted || it.weight > 0 || it.reps > 0 }
        
        val endTime = System.currentTimeMillis()
        val durationMillis = maxOf(0L, endTime - session.startTime)
        val totalVolume = completedSets.sumOf { it.weight * it.reps }
        
        val updatedSession = session.copy(
            endTime = endTime,
            status = WorkoutStatus.COMPLETED,
            notes = notes,
            durationMillis = durationMillis,
            totalVolume = totalVolume
        )
        sessionRepository.updateSession(updatedSession)

        // PR Detection (compared against previous completed sessions)
        val prsDetected = mutableListOf<String>()
        completedSets.groupBy { it.exerciseName }.forEach { (exerciseName, exerciseSets) ->
            val maxWeightThisSession = exerciseSets.maxOfOrNull { it.weight } ?: 0.0
            val maxRepsAtMaxWeight = exerciseSets.filter { it.weight == maxWeightThisSession }.maxOfOrNull { it.reps } ?: 0
            
            val prevMaxWeight = routineRepository.getPreviousMaxWeightForExercise(session.profileId, exerciseName, sessionId)
            val prevMaxReps = routineRepository.getPreviousMaxRepsForExercise(session.profileId, exerciseName, sessionId)

            if (maxWeightThisSession > 0 && (prevMaxWeight == null || maxWeightThisSession > prevMaxWeight.toDouble())) {
                prsDetected.add("New Max Weight for $exerciseName: ${maxWeightThisSession}kg!")
            } else if (maxRepsAtMaxWeight > 0 && (prevMaxReps == null || (prevMaxWeight != null && maxWeightThisSession == prevMaxWeight.toDouble() && maxRepsAtMaxWeight > prevMaxReps))) {
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
