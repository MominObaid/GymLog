package com.example.gymlog

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {
    //    private val repository: WorkoutRepository
    val allWorkouts: LiveData<List<Workout>> = repository.allWorkouts

//    init {
//        val workoutDao = WorkoutDatabase.getDatabase(application).workoutDao()
//        repository = WorkoutRepository(workoutDao)
//        allWorkouts = repository.allWorkouts
//    }

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
}