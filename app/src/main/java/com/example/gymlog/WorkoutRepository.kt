package com.example.gymlog

import androidx.lifecycle.LiveData

class WorkoutRepository(private val workoutDao: WorkoutDao) {
    val allWorkouts: LiveData<List<Workout>> = workoutDao.getAllWorkouts()

    suspend fun insert(workout: Workout){
        workoutDao.insert(workout)
    }
    suspend fun delete(workout: Workout){
        workoutDao.delete(workout)
    }
    suspend fun deleteAll(){
        workoutDao.deleteAll()
    }
}
//knowledge of data visualisation techniques using Power BI, as well as organise data and creative dashboards.