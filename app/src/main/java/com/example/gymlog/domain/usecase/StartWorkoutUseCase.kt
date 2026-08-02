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
        val sessionId = sessionRepository.startSession(profileId, routineId)
        
        // Pre-populate sets based on routine exercises
        val exercises = routineRepository.getExercisesForRoutineSync(routineId)
        exercises.forEach { exercise ->
            repeat(exercise.targetSets) { index ->
                sessionRepository.insertSet(
                    WorkoutSetEntity(
                        sessionId = sessionId.toInt(),
                        exerciseName = exercise.exerciseName,
                        muscleGroup = exercise.muscleGroup,
                        setNumber = index + 1,
                        weight = 0.0,
                        reps = exercise.targetReps,
                        isCompleted = false
                    )
                )
            }
        }
        
        return sessionId
    }
}
