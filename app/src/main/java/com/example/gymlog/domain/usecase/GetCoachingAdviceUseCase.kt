package com.example.gymlog.domain.usecase

import com.example.gymlog.AiAssistantManager
import com.example.gymlog.RoutineRepository
import com.example.gymlog.model.UserProfile
import javax.inject.Inject

class GetCoachingAdviceUseCase @Inject constructor(
    private val aiManager: AiAssistantManager,
    private val repository: RoutineRepository
) {
    suspend operator fun invoke(userMessage: String): String? {
        val profile = repository.getProfile() ?: UserProfile()
        val totalWorkouts = repository.getWorkoutCount(profile.id)
        val strongest = repository.getStrongestExercises(profile.id)
        
        val contextPrompt = """
            You are an expert fitness coach. 
            User Profile: Goal=${profile.goal}, Level=${profile.experienceLevel}, Days/Week=${profile.workoutDaysPerWeek}, Equipment=${profile.availableEquipment}.
            Stats: Total Workouts=$totalWorkouts.
            Strongest Exercises: ${strongest.joinToString { "${it.exerciseName} (${it.maxWeight}kg)" }}.
            
            User says: $userMessage
            
            Provide personalized, data-driven coaching advice.
        """.trimIndent()

        return aiManager.getRawAdvice(contextPrompt)
    }
}
