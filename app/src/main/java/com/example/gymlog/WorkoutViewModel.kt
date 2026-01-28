package com.example.gymlog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutViewModel (application: Application) : AndroidViewModel(application) {
    private val repository: WorkoutRepository
    val allWorkouts: LiveData<List<Workout>>

      //LiveData to hold the list of Exercise from the API
    val apiExercises: MutableLiveData<List<ApiExercise>> = MutableLiveData()

    //LiveData for error handling
    val apiError: MutableLiveData<String> = MutableLiveData()

    fun fetchExercisesFromApi(){
        viewModelScope.launch(Dispatchers.IO) { //Runs on the Background thread.
            try {
                val response = RetrofitInstance.api.getExercises()
                if (response.isSuccessful && response.body() != null){
                    //Post the successful result to the LiveData
                    apiExercises.postValue(response.body()!!.results)
                }else{
                    //Post an Error message
                    apiError.postValue("API Error: ${response.message()}")
                }
            } catch (e: Exception){
                //Post an error message for network exception
                apiError.postValue("Network Error: ${e.message}")
            }
        }
    }

    init {
        val workoutDao = WorkoutDatabase.getDatabase(application).workoutDao()
        repository = WorkoutRepository(workoutDao)
        allWorkouts = repository.allWorkouts
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

        fun getWorkoutById(id: Int): LiveData<Workout>{
        return repository.getWorkoutById(id)
    }
//    fun getWorkoutById(workout: Workout) = viewModelScope.launch(Dispatchers.IO) {
//        repository.getWorkoutById(id = workout.id)
//    }
    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }
    fun update (workout: Workout)= viewModelScope.launch(Dispatchers.IO) {
        repository.update(workout)
    }
    fun getWorkoutById(id: Int): LiveData<Workout> {
        return repository.getWorkoutById(id)
    }

}