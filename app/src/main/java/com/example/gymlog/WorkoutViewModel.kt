package com.example.gymlog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.gymlog.api.ApiExercise
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
    private val aiManager: AiAssistantManager
) : ViewModel() {
    val allWorkouts: LiveData<List<Workout>> = repository.allWorkouts

    private val _filterParams = MutableLiveData(FilterParams("", null))
    val filteredWorkouts: LiveData<List<Workout>> = _filterParams.switchMap { params ->
        repository.getFilteredWorkouts(params.query, params.sinceDate)
    }

    fun updateFilter(query: String? = null, sinceDate: Long? = null, clearDate: Boolean = false) {
        val current = _filterParams.value ?: FilterParams("", null)
        _filterParams.value = FilterParams(
            query = query ?: current.query,
            sinceDate = if (clearDate) null else (sinceDate ?: current.sinceDate)
        )
    }

    data class FilterParams(val query: String, val sinceDate: Long?)

    //LiveData to hold the list of Exercise from the API
    val apiExercises: MutableLiveData<List<ApiExercise>> = MutableLiveData()

    //LiveData for error handling
    val apiError: MutableLiveData<String> = MutableLiveData()
    
    private val _aiResponseEvent = MutableSharedFlow<String>()
    val aiResponseEvent: SharedFlow<String> = _aiResponseEvent.asSharedFlow()

    fun askAi(prompt : String){
        viewModelScope.launch {
            val result = aiManager.getWorkoutAdvice(prompt)
            if (result != null) {
                _aiResponseEvent.emit(result)
            }
        }
    }

    init {
        fetchExercisesFromApi()
    }

    fun fetchExercisesFromApi() {
        viewModelScope.launch { // viewModelScope uses Dispatchers.Main.immediate by default
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
        repository.insert(workout)
    }

    fun delete(workout: Workout) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(workout)
    }

    fun update(workout: Workout) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(workout)
    }

    fun getWorkoutById(id: Int): LiveData<Workout> {
        return repository.getWorkoutById(id)
    }

    fun getRecentWorkouts(sinceDate: Long): LiveData<List<Workout>> {
        return repository.getRecentWorkouts(sinceDate)
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }

    fun getWorkoutHistory(exerciseName: String): LiveData<List<Workout>> {
        return repository.getWorkoutHistory(exerciseName)
    }
}
