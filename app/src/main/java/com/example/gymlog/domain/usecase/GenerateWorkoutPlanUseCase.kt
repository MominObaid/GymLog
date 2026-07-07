package com.example.gymlog.domain.usecase

import android.util.Log
import com.example.gymlog.AiAssistantManager
import com.example.gymlog.RoutineRepository
import com.example.gymlog.model.RoutineEntity
import com.example.gymlog.model.RoutineExerciseEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import javax.inject.Inject

class GenerateWorkoutPlanUseCase @Inject constructor(
    private val aiManager: AiAssistantManager,
    private val repository: RoutineRepository
) {
    data class AiRoutine(val name: String, val goal: String, val exercises: List<AiExercise>)
    data class AiExercise(val exerciseName: String, val muscleGroup: String, val targetSets: Int, val targetReps: Int)

    suspend operator fun invoke(profileId: Int, request: String): Boolean {
        val repositoryProfile = repository.getProfile() ?: com.example.gymlog.model.UserProfile()
        val prompt = """
            Create a structured workout plan for a user.
            User Profile: Goal=${repositoryProfile.goal}, Level=${repositoryProfile.experienceLevel}, Equipment=${repositoryProfile.availableEquipment}.
            User Request: $request
            
            Return ONLY a valid JSON array of objects. Do not include any markdown formatting like ```json or any conversational text.
            Structure:
            [
              {
                "name": "Routine Name",
                "goal": "Routine Goal",
                "exercises": [
                  {
                    "exerciseName": "Exercise Name",
                    "muscleGroup": "Chest/Back/Legs/Shoulders/Arms/Core/Other",
                    "targetSets": 3,
                    "targetReps": 10
                  }
                ]
              }
            ]
        """.trimIndent()

        val rawResponse = aiManager.getRawAdvice(prompt) ?: return false
        
        return try {
            val start = rawResponse.indexOf("[")
            val end = rawResponse.lastIndexOf("]")
            if (start == -1 || end == -1) return false
            
            val fullJson = rawResponse.substring(start, end + 1)
            
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val type = Types.newParameterizedType(List::class.java, AiRoutine::class.java)
            val adapter = moshi.adapter<List<AiRoutine>>(type)
            val routinesList = adapter.fromJson(fullJson)
            
            routinesList?.forEach { aiRoutine ->
                if (aiRoutine.exercises.isNotEmpty()) {
                    repository.insertRoutineWithExercises(
                        RoutineEntity(
                            profileId = profileId,
                            name = aiRoutine.name,
                            goal = aiRoutine.goal,
                            createdAt = System.currentTimeMillis(),
                            restTimerSeconds = 90
                        ),
                        aiRoutine.exercises.mapIndexed { index, ex ->
                            RoutineExerciseEntity(
                                routineId = 0,
                                exerciseName = ex.exerciseName,
                                muscleGroup = ex.muscleGroup,
                                targetSets = ex.targetSets,
                                targetReps = ex.targetReps,
                                exerciseOrder = index
                            )
                        }
                    )
                }
            }
            true
        } catch (e: Exception) {
            Log.e("AI_PARSE", "Failed to parse AI plan: ${e.message}")
            false
        }
    }
}
