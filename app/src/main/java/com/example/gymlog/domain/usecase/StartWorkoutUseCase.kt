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
        // Complete any stale active session for a different routine
        val activeSession = sessionRepository.getActiveSessionSync()
        if (activeSession != null && activeSession.routineId != routineId) {
            sessionRepository.updateSession(
                activeSession.copy(
                    status = com.example.gymlog.model.WorkoutStatus.COMPLETED,
                    endTime = System.currentTimeMillis()
                )
            )
        }

        val sessionId = sessionRepository.startSession(profileId, routineId)
        
        // Pre-populate sets based on routine exercises with last used weight
        val exercises = routineRepository.getExercisesForRoutineSync(routineId)
        exercises.forEach { exercise ->
            val lastWeight = routineRepository.getMaxWeightForExercise(profileId, exercise.exerciseName)?.toDouble() ?: 0.0
            repeat(exercise.targetSets) { index ->
                sessionRepository.insertSet(
                    WorkoutSetEntity(
                        sessionId = sessionId.toInt(),
                        exerciseName = exercise.exerciseName,
                        muscleGroup = exercise.muscleGroup,
                        setNumber = index + 1,
                        weight = lastWeight,
                        reps = exercise.targetReps,
                        isCompleted = false
                    )
                )
            }
        }
        
        return sessionId
    }
}
