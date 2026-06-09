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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
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
    val recentSessions = repository.getRecentSessions().asLiveData()

    private val _aiPlanGenerated = MutableLiveData<Boolean>()
    val aiPlanGenerated: LiveData<Boolean> = _aiPlanGenerated

    // Dashboard Data
    private val _dashboardHeader = MutableLiveData<DashboardHeader>()
    val dashboardHeader: LiveData<DashboardHeader> = _dashboardHeader

    private val _todayWorkout = MutableLiveData<TodayWorkout?>()
    val todayWorkout: LiveData<TodayWorkout?> = _todayWorkout

    private val _dashboardStats = MutableLiveData<DashboardStats>()
    val dashboardStats: LiveData<DashboardStats> = _dashboardStats

    data class DashboardHeader(val userName: String, val greeting: String)
    data class TodayWorkout(val routineId: Int, val routineName: String, val exerciseCount: Int, val durationMinutes: Int)
    data class DashboardStats(val weeklyVolume: Float, val workoutCount: Int, val favoriteExercise: String)

    fun updateDashboardData() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getProfile()
            val userName = profile?.name ?: "Athlete"
            _dashboardHeader.postValue(DashboardHeader(userName, getGreeting()))

            // Today's workout logic: Pick the first routine for now, or most frequent
            // In a real app, this might be based on a schedule
            val routines = repository.allRoutines.firstOrNull()
            if (!routines.isNullOrEmpty()) {
                val routine = routines.first()
                // Need a way to get exercise count for this routine
                // Flow based getExercisesForRoutine can be converted or we can just count from a direct query if added
                // For simplicity, let's just assume we have it or use a default
                _todayWorkout.postValue(TodayWorkout(routine.id, routine.name, 0, 45)) 
                // We should ideally fetch the exercise count
            } else {
                _todayWorkout.postValue(null)
            }

            val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            val volume = repository.getTotalVolumeSince(oneWeekAgo) ?: 0f
            val count = repository.getWorkoutCountSince(oneWeekAgo)
            val fav = repository.getFavoriteExercise() ?: "None"
            _dashboardStats.postValue(DashboardStats(volume, count, fav))
        }
    }

    private fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

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
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertProfile(profile)
            _userProfile.postValue(profile)
        }
    }

    fun loadProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            _userProfile.postValue(repository.getProfile())
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
        viewModelScope.launch(Dispatchers.IO) {
            updateMilestones()
            loadHealthData()
            loadProfile()
            updateDashboardData()
        }
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
        viewModelScope.launch(Dispatchers.IO) {
            _totalWorkouts.postValue(repository.getWorkoutCount())
            val times = repository.getAllSessionTimes()
            _streak.postValue(StreakCalculator.calculateStreak(times))
            _longestStreak.postValue(StreakCalculator.calculateLongestStreak(times))

            val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            _weeklyVolume.postValue(repository.getTotalVolumeSince(oneWeekAgo) ?: 0f)
            
            val strongOnes = repository.getStrongestExercises()
            _strongestExercises.postValue(strongOnes)

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
            _plateaus.postValue(plateauList)
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
