package com.example.gymlog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {
//    private val repository: WorkoutRepository
    val allWorkouts: LiveData<List<Workout>>
    get() = repository.allWorkouts

    //LiveData to hold the list of Exercise from the API
    val apiExercises: MutableLiveData<List<ApiExercise>> = MutableLiveData()

    //LiveData for error handling
    val apiError: MutableLiveData<String> = MutableLiveData()

    init {
        viewModelScope.launch {
        repository.fetchFromApi()

//        val workoutDao = WorkoutDatabase.getDatabase()
//        repository = WorkoutRepository(workoutDao, ApiService)
//        allWorkouts = repository.allWorkouts
    }
    }

    fun fetchExercisesFromApi() {
        viewModelScope.launch { //Runs on the Background thread.
            when(val result = repository.fetchFromApi()){
                is WorkoutRepository.ApiResult.Success<List<ApiExercise>> -> {
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

    // Functions to interact with the repository, wrapped in Coroutines.
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
}