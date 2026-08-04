package com.example.gymlog.domain.usecase

import com.example.gymlog.RoutineRepository
import com.example.gymlog.health.HealthConnectManager
import com.example.gymlog.model.WorkoutSetEntity
import com.example.gymlog.model.WorkoutSessionEntity
import java.time.Instant
import javax.inject.Inject

class SaveWorkoutSessionUseCase @Inject constructor(
    private val repository: RoutineRepository,
    private val healthConnectManager: HealthConnectManager
) {
    data class SaveResult(
        val prMessage: String?
    )

    suspend operator fun invoke(
        profileId: Int,
        routineId: Int,
        startTime: Long,
        endTime: Long,
        notes: String?,
        sessionExercises: List<WorkoutSetEntity>
    ): SaveResult {
        val sessionId = repository.insertSession(
            WorkoutSessionEntity(
                profileId = profileId,
                routineId = routineId,
                startTime = startTime,
                endTime = endTime,
                notes = notes
            )
        ).toInt()

        val prsDetected = mutableListOf<String>()

        sessionExercises.forEach { exercise ->
            val prevMaxWeight = repository.getMaxWeightForExercise(profileId, exercise.exerciseName)
            val prevMaxReps = repository.getMaxRepsForExercise(profileId, exercise.exerciseName)

            if (prevMaxWeight == null || exercise.weight > prevMaxWeight.toDouble()) {
                prsDetected.add("New Max Weight for ${exercise.exerciseName}: ${exercise.weight}kg!")
            } else if (prevMaxReps == null || (exercise.weight == prevMaxWeight.toDouble() && exercise.reps > prevMaxReps)) {
                prsDetected.add("New Rep Record for ${exercise.exerciseName}: ${exercise.reps} reps at ${exercise.weight}kg!")
            }

            repository.insertSessionExercise(exercise.copy(sessionId = sessionId))
        }

        // Write to Health Connect
        if (healthConnectManager.isHealthConnectAvailable() && healthConnectManager.hasAllPermissions()) {
            val routine = repository.getRoutineById(routineId)
            healthConnectManager.writeWorkoutSession(
                startTime = Instant.ofEpochMilli(startTime),
                endTime = Instant.ofEpochMilli(endTime),
                title = routine?.name ?: "Gym Workout",
                notes = notes
            )
        }

        return SaveResult(
            prMessage = if (prsDetected.isNotEmpty()) prsDetected.joinToString("\n") else null
        )
    }
}
