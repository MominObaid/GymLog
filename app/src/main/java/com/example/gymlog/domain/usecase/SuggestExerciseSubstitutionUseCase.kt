package com.example.gymlog.domain.usecase

import com.example.gymlog.AiAssistantManager
import com.example.gymlog.RoutineRepository
import javax.inject.Inject

class SuggestExerciseSubstitutionUseCase @Inject constructor(
    private val aiManager: AiAssistantManager,
    private val repository: RoutineRepository
) {
    suspend operator fun invoke(profileId: Int, exerciseToReplace: String): String? {
        val profile = repository.getProfile()
        val equipment = profile?.availableEquipment ?: "Full Gym"
        
        val prompt = """
            The user wants to replace '$exerciseToReplace' because it's unavailable.
            Available Equipment: $equipment.
            User Goal: ${profile?.goal}.
            
            Suggest 3 effective alternative exercises that target the same muscle groups.
            
            Return ONLY a JSON array of strings:
            ["Suggestion 1", "Suggestion 2", "Suggestion 3"]
        """.trimIndent()

        return aiManager.getRawAdvice(prompt)
    }
}
