package com.example.gymlog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymlog.api.ApiExercise
import com.example.gymlog.model.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {
    val allWorkouts: LiveData<List<Workout>> = repository.allWorkouts

    //LiveData to hold the list of Exercise from the API
    val apiExercises: MutableLiveData<List<ApiExercise>> = MutableLiveData()

    //LiveData for error handling
    val apiError: MutableLiveData<String> = MutableLiveData()
    private val aiManager = AiAssistantManager()
    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String> = _aiResponse

    fun clearAiResponse(){
        _aiResponse.value = null // Clear the value
    }

    fun askAi(prompt : String){
        viewModelScope.launch {
            val result = aiManager.getWorkoutAdvice(prompt)
            _aiResponse.postValue(result)
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

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }

    fun getWorkoutHistory(exerciseName: String): LiveData<List<Workout>> {
        return repository.getWorkoutHistory(exerciseName)
    }
}
