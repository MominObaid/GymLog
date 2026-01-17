package com.example.gymlog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WorkoutRepository
    val allWorkouts: LiveData<List<Workout>>

    init {
        val workoutDao = WorkoutDatabase.getDatabase(application).workoutDao()
        repository = WorkoutRepository(workoutDao)
        allWorkouts = repository.allWorkouts
    }

    fun insert(workout: Workout) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(workout)
    }

    fun delete(workout: Workout) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(workout)
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()

    }
}