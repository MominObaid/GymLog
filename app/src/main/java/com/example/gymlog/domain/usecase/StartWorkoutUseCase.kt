package com.example.gymlog.domain.usecase

import com.example.gymlog.RoutineRepository
import com.example.gymlog.SessionRepository
import com.example.gymlog.model.WorkoutSetEntity
import javax.inject.Inject

class StartWorkoutUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val routineRepository: RoutineRepository
) {
    suspend operator fun invoke(profileId: Int, routineId: Int): Long {
        // Check if an active session already exists
        val activeSession = sessionRepository.getActiveSessionSync()
        if (activeSession != null) {
            if (activeSession.routineId == routineId) {
                val existingSets = sessionRepository.getSetsForSessionSync(activeSession.id)
                if (existingSets.isNotEmpty()) {
                    return activeSession.id.toLong()
                }
            } else {
                // Complete stale session for a different routine
                sessionRepository.updateSession(
                    activeSession.copy(
                        status = com.example.gymlog.model.WorkoutStatus.COMPLETED,
                        endTime = System.currentTimeMillis()
                    )
                )
            }
        }

        val sessionId = sessionRepository.startSession(profileId, routineId)
        
        // Pre-populate sets based on routine exercises with last used weight
        val exercises = routineRepository.getExercisesForRoutineSync(routineId)
        exercises.forEach { exercise ->
            val lastWeight = routineRepository.getMaxWeightForExercise(profileId, exercise.exerciseName)?.toDouble() ?: 0.0
            val numSets = if (exercise.targetSets > 0) exercise.targetSets else 3
            val numReps = if (exercise.targetReps > 0) exercise.targetReps else 10
            repeat(numSets) { index ->
                sessionRepository.insertSet(
                    WorkoutSetEntity(
                        sessionId = sessionId.toInt(),
                        exerciseName = exercise.exerciseName,
                        muscleGroup = exercise.muscleGroup,
                        setNumber = index + 1,
                        weight = lastWeight,
                        reps = numReps,
                        isCompleted = false
                    )
                )
            }
        }
        return sessionId
    }
}
