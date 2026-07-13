package com.example.gymlog.domain.usecase

import com.example.gymlog.AiAssistantManager
import com.example.gymlog.RoutineRepository
import javax.inject.Inject

class PredictFitnessProgressUseCase @Inject constructor(
    private val aiManager: AiAssistantManager,
    private val repository: RoutineRepository
) {
    suspend operator fun invoke(profileId: Int): String? {
        val strongest = repository.getStrongestExercises(profileId)
        if (strongest.isEmpty()) return null

        val historyData = strongest.map { stat ->
            val history = repository.getWeightHistoryForExercise(profileId, stat.exerciseName)
            "${stat.exerciseName}: History of max weights (recent first): ${history.take(5).joinToString(", ")}"
        }.joinToString("\n")

        val prompt = """
            Based on the following workout history, predict the user's progress for the next 8 weeks.
            History:
            $historyData
            
            Provide a data-driven prediction for their top 3 exercises.
            
            Return ONLY a JSON object with this structure:
            {
              "predictions": [
                {
                  "exercise": "Bench Press",
                  "currentMax": 70,
                  "predictedMax": 80,
                  "weeks": 8,
                  "confidence": "High/Medium/Low"
                }
              ]
            }
        """.trimIndent()

        return aiManager.getRawAdvice(prompt)
    }
}
