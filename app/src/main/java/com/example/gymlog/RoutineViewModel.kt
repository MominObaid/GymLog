package com.example.gymlog

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.gymlog.health.HealthConnectManager
import com.example.gymlog.model.RoutineEntity
import com.example.gymlog.model.RoutineExerciseEntity
import com.example.gymlog.model.SessionExerciseEntity
import com.example.gymlog.model.WorkoutSessionEntity
import com.example.gymlog.utils.StreakCalculator
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val repository: RoutineRepository,
    private val healthConnectManager: HealthConnectManager,
    private val aiManager: AiAssistantManager
) : ViewModel() {

    val allRoutines = repository.allRoutines.asLiveData()
    val allSessions = repository.getAllSessions().asLiveData()

    private val _aiPlanGenerated = MutableLiveData<Boolean>()
    val aiPlanGenerated: LiveData<Boolean> = _aiPlanGenerated

    fun generateAiPlan(request: String) {
        viewModelScope.launch {
            val jsonResponse = aiManager.generateWorkoutPlan(request)
            if (jsonResponse != null) {
                try {
                    // Extract JSON array more robustly
                    val start = jsonResponse.indexOf("[")
                    val end = jsonResponse.lastIndexOf("]")
                    if (start == -1 || end == -1) {
                        Log.e("AI_PARSE", "No JSON array found in response: $jsonResponse")
                        return@launch
                    }
                    val fullJson = jsonResponse.substring(start, end + 1)
                    
                    val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                    val type = Types.newParameterizedType(List::class.java, AiRoutine::class.java)
                    val adapter = moshi.adapter<List<AiRoutine>>(type)
                    val routinesList = adapter.fromJson(fullJson)
                    
                    routinesList?.forEach { aiRoutine ->
                        if (aiRoutine.exercises.isNotEmpty()) {
                            insertRoutine(
                                aiRoutine.name,
                                aiRoutine.goal,
                                aiRoutine.exercises.mapIndexed { index, ex ->
                                    RoutineExerciseEntity(
                                        routineId = 0,
                                        exerciseName = ex.exerciseName,
                                        targetSets = ex.targetSets,
                                        targetReps = ex.targetReps,
                                        exerciseOrder = index
                                    )
                                },
                                restTimer = 90 // Default for AI plans
                            )
                        } else {
                            Log.w("AI_GEN", "Skipping routine ${aiRoutine.name} because it has no exercises")
                        }
                    }
                    _aiPlanGenerated.value = true
                } catch (e: Exception) {
                    Log.e("AI_PARSE", "Failed to parse AI plan: ${e.message}")
                }
            }
        }
    }

    data class AiRoutine(val name: String, val goal: String, val exercises: List<AiExercise>)
    data class AiExercise(val exerciseName: String, val targetSets: Int, val targetReps: Int)

    private val _userProfile = MutableLiveData<com.example.gymlog.model.UserProfile?>()
    val userProfile: LiveData<com.example.gymlog.model.UserProfile?> = _userProfile

    fun updateProfile(profile: com.example.gymlog.model.UserProfile) {
        viewModelScope.launch {
            repository.insertProfile(profile)
            _userProfile.value = profile
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _userProfile.value = repository.getProfile()
        }
    }

    private val _prEvent = MutableLiveData<String?>()
    val prEvent: LiveData<String?> = _prEvent

    private val _streak = MutableLiveData<Int>()
    val streak: LiveData<Int> = _streak

    private val _longestStreak = MutableLiveData<Int>()
    val longestStreak: LiveData<Int> = _longestStreak

    private val _totalWorkouts = MutableLiveData<Int>()
    val totalWorkouts: LiveData<Int> = _totalWorkouts

    private val _weeklyVolume = MutableLiveData<Float>()
    val weeklyVolume: LiveData<Float> = _weeklyVolume

    private val _strongestExercises = MutableLiveData<List<com.example.gymlog.model.ExerciseStat>>()
    val strongestExercises: LiveData<List<com.example.gymlog.model.ExerciseStat>> = _strongestExercises

    private val _plateaus = MutableLiveData<List<String>>()
    val plateaus: LiveData<List<String>> = _plateaus

    private val _todaySteps = MutableLiveData<Long>()
    val todaySteps: LiveData<Long> = _todaySteps

    private val _latestWeight = MutableLiveData<Double?>()
    val latestWeight: LiveData<Double?> = _latestWeight

    val volumeHistory = repository.getVolumeHistory().asLiveData()

    init {
        updateMilestones()
        loadHealthData()
        loadProfile()
    }

    fun loadHealthData() {
        if (healthConnectManager.isHealthConnectAvailable()) {
            viewModelScope.launch {
                if (healthConnectManager.hasAllPermissions()) {
                    val start = Instant.now().truncatedTo(ChronoUnit.DAYS)
                    val end = Instant.now()
                    _todaySteps.value = healthConnectManager.readSteps(start, end)
                    _latestWeight.value = healthConnectManager.readLatestWeight()
                }
            }
        }
    }

    fun updateMilestones() {
        viewModelScope.launch {
            _totalWorkouts.value = repository.getWorkoutCount()
            val times = repository.getAllSessionTimes()
            _streak.value = StreakCalculator.calculateStreak(times)
            _longestStreak.value = StreakCalculator.calculateLongestStreak(times)

            val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            _weeklyVolume.value = repository.getTotalVolumeSince(oneWeekAgo) ?: 0f
            val strongOnes = repository.getStrongestExercises()
            _strongestExercises.value = strongOnes

            // Plateau Detection
            val plateauList = mutableListOf<String>()
            strongOnes.forEach { stat ->
                 val history = repository.getWeightHistoryForExercise(stat.exerciseName)
                 if (history.size >= 4) {
                     val recent = history.take(4)
                     if (recent.distinct().size == 1) {
                         plateauList.add("${stat.exerciseName} has stalled at ${stat.maxWeight}kg")
                     }
                 }
            }
            _plateaus.value = plateauList
        }
    }

    fun getExercisesForRoutine(routineId: Int) = 
        repository.getExercisesForRoutine(routineId).asLiveData()

    suspend fun getRoutineById(routineId: Int): RoutineEntity? {
        return repository.getRoutineById(routineId)
    }

    fun deleteRoutine(routineId: Int) {
        viewModelScope.launch {
            repository.deleteRoutine(routineId)
        }
    }

    fun deleteSession(sessionId: Int) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            updateMilestones()
        }
    }

    fun insertRoutine(name: String, goal: String, exercises: List<RoutineExerciseEntity>, restTimer: Int = 90) {
        viewModelScope.launch {
            repository.insertRoutineWithExercises(
                RoutineEntity(name = name, goal = goal, createdAt = System.currentTimeMillis(), restTimerSeconds = restTimer),
                exercises
            )
        }
    }

    fun updateRoutine(id: Int, name: String, goal: String, exercises: List<RoutineExerciseEntity>, restTimer: Int = 90) {
        viewModelScope.launch {
            repository.updateRoutineWithExercises(
                RoutineEntity(id = id, name = name, goal = goal, createdAt = System.currentTimeMillis(), restTimerSeconds = restTimer),
                exercises
            )
        }
    }

    private val _sessionSaved = MutableLiveData<Boolean>()
    val sessionSaved: LiveData<Boolean> = _sessionSaved

    fun saveWorkoutSession(routineId: Int, startTime: Long, endTime: Long, notes: String?, sessionExercises: List<SessionExerciseEntity>) {
        viewModelScope.launch {
            val sessionId = repository.insertSession(
                WorkoutSessionEntity(routineId = routineId, startTime = startTime, endTime = endTime, notes = notes)
            ).toInt()

            val prsDetected = mutableListOf<String>()

            sessionExercises.forEach { exercise ->
                // Check for PRs before inserting
                val prevMaxWeight = repository.getMaxWeightForExercise(exercise.exerciseName)
                val prevMaxReps = repository.getMaxRepsForExercise(exercise.exerciseName)

                if (prevMaxWeight == null || exercise.weight > prevMaxWeight) {
                    prsDetected.add("New Max Weight for ${exercise.exerciseName}: ${exercise.weight}kg!")
                } else if (prevMaxReps == null || (exercise.weight == prevMaxWeight && exercise.reps > prevMaxReps)) {
                    prsDetected.add("New Rep Record for ${exercise.exerciseName}: ${exercise.reps} reps at ${exercise.weight}kg!")
                }

                repository.insertSessionExercise(exercise.copy(sessionId = sessionId))
            }

            if (prsDetected.isNotEmpty()) {
                _prEvent.value = prsDetected.joinToString("\n")
            }
            
            // Write to Health Connect
            if (healthConnectManager.isHealthConnectAvailable() && healthConnectManager.hasAllPermissions()) {
                val routine = allRoutines.value?.find { it.id == routineId }
                healthConnectManager.writeWorkoutSession(
                    startTime = Instant.ofEpochMilli(startTime),
                    endTime = Instant.ofEpochMilli(endTime),
                    title = routine?.name ?: "Gym Workout",
                    notes = notes
                )
            }

            updateMilestones()
            _sessionSaved.value = true
        }
    }

    fun resetSessionSaved() {
        _sessionSaved.value = false
    }

    fun resetAiPlanFlag() {
        _aiPlanGenerated.value = false
    }

    fun resetPrEvent() {
        _prEvent.value = null
    }
}
