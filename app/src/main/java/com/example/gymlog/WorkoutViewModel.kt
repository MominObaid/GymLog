package com.example.gymlog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.gymlog.api.ApiExercise
import com.example.gymlog.domain.usecase.GetCoachingAdviceUseCase
import com.example.gymlog.model.Workout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    private val routineRepository: RoutineRepository,
    private val getCoachingAdviceUseCase: GetCoachingAdviceUseCase
) : ViewModel() {

    private val activeProfileId = routineRepository.getAllProfiles().asLiveData().switchMap { profiles ->
        val active = profiles.find { it.isActive }?.id ?: profiles.firstOrNull()?.id ?: 0
        MutableLiveData(active)
    }

    val allWorkouts: LiveData<List<Workout>> = activeProfileId.switchMap { id ->
        repository.getAllWorkouts(id).asLiveData()
    }

    private val _filterParams = MutableLiveData(FilterParams("", null))
    val filteredWorkouts: LiveData<List<Workout>> = activeProfileId.switchMap { profileId ->
        _filterParams.switchMap { params ->
            repository.getFilteredWorkouts(profileId, params.query, params.sinceDate).asLiveData()
        }
    }

    fun updateFilter(query: String? = null, sinceDate: Long? = null, clearDate: Boolean = false) {
        val current = _filterParams.value ?: FilterParams("", null)
        _filterParams.value = FilterParams(
            query = query ?: current.query,
            sinceDate = if (clearDate) null else (sinceDate ?: current.sinceDate)
        )
    }

    data class FilterParams(val query: String, val sinceDate: Long?)

    val apiExercises: MutableLiveData<List<ApiExercise>> = MutableLiveData()
    val apiError: MutableLiveData<String> = MutableLiveData()
    
    private val _aiResponseEvent = MutableSharedFlow<String>()
    val aiResponseEvent: SharedFlow<String> = _aiResponseEvent.asSharedFlow()

    fun askAi(prompt : String){
        viewModelScope.launch {
            val result = getCoachingAdviceUseCase(prompt)
            if (result != null) {
                _aiResponseEvent.emit(result)
            }
        }
    }

    init {
        fetchExercisesFromApi()
    }

    fun fetchExercisesFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = repository.fetchFromApi()) {
                is WorkoutRepository.ApiResult.Success -> {
                    apiExercises.postValue(result.data)
                }
                is WorkoutRepository.ApiResult.Error -> {
                    apiError.postValue(result.message)
                }
            }
        }
    }

    fun insert(workout: Workout) = viewModelScope.launch(Dispatchers.IO) {
        val profileId = activeProfileId.value ?: 0
        repository.insert(workout.copy(profileId = profileId))
    }

    fun delete(workout: Workout) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(workout)
    }

    fun update(workout: Workout) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(workout)
    }

    fun getWorkoutById(id: Int): LiveData<Workout?> {
        return repository.getWorkoutById(id).asLiveData()
    }

    fun getRecentWorkouts(sinceDate: Long): LiveData<List<Workout>> {
        val profileId = activeProfileId.value ?: 0
        return repository.getRecentWorkouts(profileId, sinceDate).asLiveData()
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        val profileId = activeProfileId.value ?: 0
        repository.deleteAll(profileId)
    }

    fun getWorkoutHistory(exerciseName: String): LiveData<List<Workout>> {
        val profileId = activeProfileId.value ?: 0
        return repository.getWorkoutHistory(profileId, exerciseName).asLiveData()
    }

    suspend fun getProfile() = repository.getProfile()
}
