package com.example.gymlog.domain.usecase

import com.example.gymlog.AiAssistantManager
import com.example.gymlog.RoutineRepository
import com.example.gymlog.model.SessionExerciseEntity
import javax.inject.Inject

class AnalyzeWorkoutPerformanceUseCase @Inject constructor(
    private val aiManager: AiAssistantManager,
    private val repository: RoutineRepository
) {
    suspend operator fun invoke(
        profileId: Int,
        routineName: String,
        sessionExercises: List<SessionExerciseEntity>,
        durationMinutes: Int
    ): String? {
        val strongest = repository.getStrongestExercises(profileId)
        val profile = repository.getProfile()
        
        val sessionSummary = sessionExercises.groupBy { it.exerciseName }
            .map { (name, sets) ->
                "$name: ${sets.size} sets, Max Weight: ${sets.maxOf { it.weight }}kg"
            }.joinToString("\n")

        val prompt = """
            Analyze this workout session and provide 3 short coaching insights.
            User Profile: Goal=${profile?.goal}, Level=${profile?.experienceLevel}.
            Workout: $routineName, Duration: ${durationMinutes}min.
            Performance:
            $sessionSummary
            
            Compare with user's top lifts: ${strongest.joinToString { "${it.exerciseName} (${it.maxWeight}kg)" }}
            
            Return ONLY a JSON object with this structure:
            {
              "insights": ["insight 1", "insight 2", "insight 3"],
              "recoveryScore": 0-100,
              "focusArea": "muscle group or technique focus"
            }
        """.trimIndent()

        return aiManager.getRawAdvice(prompt)
    }
}
