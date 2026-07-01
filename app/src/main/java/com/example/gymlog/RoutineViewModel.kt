package com.example.gymlog

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.gymlog.domain.usecase.*
import com.example.gymlog.health.HealthConnectManager
import com.example.gymlog.model.RoutineEntity
import com.example.gymlog.model.RoutineExerciseEntity
import com.example.gymlog.model.SessionExerciseEntity
import com.example.gymlog.utils.StreakCalculator
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val repository: RoutineRepository,
    private val healthConnectManager: HealthConnectManager,
    private val authManager: com.example.gymlog.auth.AuthManager,
    private val syncManager: com.example.gymlog.sync.CloudSyncManager,
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val updateMilestonesUseCase: UpdateMilestonesUseCase,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
    private val saveWorkoutSessionUseCase: SaveWorkoutSessionUseCase,
    private val analyzeWorkoutPerformanceUseCase: AnalyzeWorkoutPerformanceUseCase,
    private val suggestExerciseSubstitutionUseCase: SuggestExerciseSubstitutionUseCase,
    private val predictFitnessProgressUseCase: PredictFitnessProgressUseCase,
    private val getRecoveryScoreUseCase: GetRecoveryScoreUseCase,
    private val getFatigueScoreUseCase: GetFatigueScoreUseCase,
    private val getMuscleVolumeUseCase: GetMuscleVolumeUseCase,
    private val getOneRMHistoryUseCase: GetOneRMHistoryUseCase
) : ViewModel() {

    private val _userProfile = MutableLiveData<com.example.gymlog.model.UserProfile?>()
    val userProfile: LiveData<com.example.gymlog.model.UserProfile?> = _userProfile

    val allRoutines = _userProfile.switchMap { profile ->
        if (profile != null) repository.getAllRoutines(profile.id).asLiveData()
        else MutableLiveData(emptyList())
    }

    val allSessions = _userProfile.switchMap { profile ->
        if (profile != null) repository.getAllSessions(profile.id).asLiveData()
        else MutableLiveData(emptyList())
    }

    val recentSessions = _userProfile.switchMap { profile ->
        if (profile != null) repository.getRecentSessions(profile.id).asLiveData()
        else MutableLiveData(emptyList())
    }

    val volumeHistory = _userProfile.switchMap { profile ->
        if (profile != null) repository.getVolumeHistory(profile.id).asLiveData()
        else MutableLiveData(emptyList())
    }

    val allProfiles = repository.getAllProfiles().asLiveData()

    private val _aiPlanGenerated = MutableLiveData<Boolean>()
    val aiPlanGenerated: LiveData<Boolean> = _aiPlanGenerated

    // Dashboard Data
    private val _dashboardHeader = MutableLiveData<GetDashboardStatsUseCase.DashboardData>()
    val dashboardHeader: LiveData<GetDashboardStatsUseCase.DashboardData> = _dashboardHeader

    val todayWorkout: LiveData<GetDashboardStatsUseCase.TodayWorkout?> = _dashboardHeader.map { it.todayWorkout }

    // AI Features Data
    private val _workoutAnalysis = MutableLiveData<String?>()
    val workoutAnalysis: LiveData<String?> = _workoutAnalysis

    private val _exerciseSubstitutions = MutableLiveData<List<String>>()
    val exerciseSubstitutions: LiveData<List<String>> = _exerciseSubstitutions

    private val _progressPredictions = MutableLiveData<String?>()
    val progressPredictions: LiveData<String?> = _progressPredictions

    // Advanced Analytics
    private val _recoveryScore = MutableLiveData<Int>()
    val recoveryScore: LiveData<Int> = _recoveryScore

    private val _fatigueScore = MutableLiveData<Int>()
    val fatigueScore: LiveData<Int> = _fatigueScore

    val muscleVolume = _userProfile.switchMap { profile ->
        if (profile != null) getMuscleVolumeUseCase(profile.id).asLiveData()
        else MutableLiveData(emptyList())
    }

    fun updateAdvancedAnalytics() {
        val profile = _userProfile.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val recovery = getRecoveryScoreUseCase(profile.id)
            val fatigue = getFatigueScoreUseCase(profile.id)
            _recoveryScore.postValue(recovery)
            _fatigueScore.postValue(fatigue)
        }
    }

    fun getOneRMHistory(exerciseName: String) = _userProfile.switchMap { profile ->
        if (profile != null) getOneRMHistoryUseCase(profile.id, exerciseName).asLiveData()
        else MutableLiveData(emptyList())
    }

    fun updateDashboardData() {
        val profile = _userProfile.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val data = getDashboardStatsUseCase(profile.id, profile.name)
            _dashboardHeader.postValue(data)
        }
    }

    fun generateAiPlan(request: String) {
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            val success = generateWorkoutPlanUseCase(profile.id, request)
            if (success) {
                _aiPlanGenerated.value = true
            }
        }
    }

    fun analyzeLastWorkout(routineName: String, sessionExercises: List<SessionExerciseEntity>, duration: Int) {
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            val jsonResponse = analyzeWorkoutPerformanceUseCase(profile.id, routineName, sessionExercises, duration)
            if (jsonResponse != null) {
                try {
                    val start = jsonResponse.indexOf("{")
                    val end = jsonResponse.lastIndexOf("}")
                    if (start != -1 && end != -1) {
                        val fullJson = jsonResponse.substring(start, end + 1)
                        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                        val adapter = moshi.adapter(AnalysisResponse::class.java)
                        val response = adapter.fromJson(fullJson)
                        
                        val summary = """
                            Focus: ${response?.focusArea}
                            Recovery: ${response?.recoveryScore}%
                            
                            ${response?.insights?.joinToString("\n") { "• $it" }}
                        """.trimIndent()
                        _workoutAnalysis.value = summary
                    }
                } catch (e: Exception) {
                    _workoutAnalysis.value = jsonResponse // Fallback to raw if parsing fails
                }
            }
        }
    }

    data class AnalysisResponse(val insights: List<String>, val recoveryScore: Int, val focusArea: String)

    fun getSubstitutions(exerciseName: String) {
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            val jsonResponse = suggestExerciseSubstitutionUseCase(profile.id, exerciseName)
            if (jsonResponse != null) {
                try {
                    val start = jsonResponse.indexOf("[")
                    val end = jsonResponse.lastIndexOf("]")
                    if (start != -1 && end != -1) {
                        val fullJson = jsonResponse.substring(start, end + 1)
                        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                        val type = Types.newParameterizedType(List::class.java, String::class.java)
                        val adapter = moshi.adapter<List<String>>(type)
                        _exerciseSubstitutions.value = adapter.fromJson(fullJson) ?: emptyList()
                    }
                } catch (e: Exception) {
                    Log.e("AI_SUB", "Failed to parse substitutions: ${e.message}")
                }
            }
        }
    }

    fun predictProgress() {
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            val jsonResponse = predictFitnessProgressUseCase(profile.id)
            if (jsonResponse != null) {
                try {
                    val start = jsonResponse.indexOf("{")
                    val end = jsonResponse.lastIndexOf("}")
                    if (start != -1 && end != -1) {
                        val fullJson = jsonResponse.substring(start, end + 1)
                        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                        val adapter = moshi.adapter(PredictionResponse::class.java)
                        val response = adapter.fromJson(fullJson)
                        
                        val summary = response?.predictions?.joinToString("\n\n") { p ->
                            "• ${p.exercise}:\n  Target: ${p.predictedMax}kg in ${p.weeks} weeks\n  Confidence: ${p.confidence}"
                        }
                        _progressPredictions.value = summary ?: "No predictions available yet."
                    }
                } catch (e: Exception) {
                    Log.e("AI_PREDICT", "Failed to parse predictions: ${e.message}")
                    _progressPredictions.value = "Unable to generate forecast. Keep training to provide more data!"
                }
            }
        }
    }

    data class PredictionResponse(val predictions: List<Prediction>)
    data class Prediction(val exercise: String, val currentMax: Float, val predictedMax: Float, val weeks: Int, val confidence: String)

    fun updateProfile(profile: com.example.gymlog.model.UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertProfile(profile)
            loadProfile()
        }
    }

    private suspend fun ensureProfileLoaded(): com.example.gymlog.model.UserProfile? {
        var active = repository.getProfile()
        if (active == null) {
            val all = repository.getAllProfiles().first()
            if (all.isNotEmpty()) {
                repository.setActiveProfile(all.first().id)
                active = repository.getProfile()
            } else {
                val defaultProfile = com.example.gymlog.model.UserProfile(
                    name = "Athlete",
                    isActive = true,
                    avatarColor = 0xFF1976D2.toInt()
                )
                repository.insertProfile(defaultProfile)
                active = repository.getProfile()
            }
        }
        return active
    }

    fun loadProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val active = ensureProfileLoaded()
            _userProfile.postValue(active)
            updateDashboardData()
        }
    }

    fun switchProfile(profileId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setActiveProfile(profileId)
            loadProfile()
            updateMilestones()
        }
    }

    fun deleteProfile(profile: com.example.gymlog.model.UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProfile(profile)
            if (profile.isActive) {
                loadProfile()
            }
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

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val active = ensureProfileLoaded()
            _userProfile.postValue(active)
            
            updateDashboardData()
            updateMilestones()
            loadHealthData()
            updateAdvancedAnalytics()
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
        val profile = _userProfile.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val result = updateMilestonesUseCase(profile.id)
            _totalWorkouts.postValue(result.totalWorkouts)
            _streak.postValue(result.currentStreak)
            _longestStreak.postValue(result.longestStreak)
            _weeklyVolume.postValue(result.weeklyVolume)
            _strongestExercises.postValue(result.strongestExercises)
            _plateaus.postValue(result.plateaus)
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
            val profile = repository.getProfile()
            val profileId = profile?.id ?: 0

            repository.insertRoutineWithExercises(
                RoutineEntity(profileId = profileId, name = name, goal = goal, createdAt = System.currentTimeMillis(), restTimerSeconds = restTimer),
                exercises
            )
        }
    }

    fun updateRoutine(id: Int, name: String, goal: String, exercises: List<RoutineExerciseEntity>, restTimer: Int = 90) {
        viewModelScope.launch {
            val profile = repository.getProfile()
            val profileId = profile?.id ?: 0

            repository.updateRoutineWithExercises(
                RoutineEntity(id = id, profileId = profileId, name = name, goal = goal, createdAt = System.currentTimeMillis(), restTimerSeconds = restTimer),
                exercises
            )
        }
    }

    private val _sessionSaved = MutableLiveData<Boolean>()
    val sessionSaved: LiveData<Boolean> = _sessionSaved

    fun saveWorkoutSession(routineId: Int, startTime: Long, endTime: Long, notes: String?, sessionExercises: List<SessionExerciseEntity>) {
        val profileId = _userProfile.value?.id ?: 0
        viewModelScope.launch {
            val result = saveWorkoutSessionUseCase(
                profileId = profileId,
                routineId = routineId,
                startTime = startTime,
                endTime = endTime,
                notes = notes,
                sessionExercises = sessionExercises
            )
            
            _prEvent.postValue(result.prMessage)
            updateMilestones()
            _sessionSaved.postValue(true)
        }
    }

    fun syncData() {
        if (syncManager.isEnabled()) {
            viewModelScope.launch(Dispatchers.IO) {
                syncManager.syncToCloud()
            }
        }
    }

    val isCloudEnabled: Boolean
        get() = syncManager.isEnabled()

    fun resetSessionSaved() {
        _sessionSaved.value = false
    }

    fun resetAiPlanFlag() {
        _aiPlanGenerated.value = false
    }

    fun resetPrEvent() {
        _prEvent.value = null
    }

    fun resetAnalysis() {
        _workoutAnalysis.value = null
    }
}
