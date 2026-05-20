package com.example.gymlog

import androidx.lifecycle.LiveData
import com.example.gymlog.api.ApiExercise
import com.example.gymlog.api.ApiService
import com.example.gymlog.model.Workout
import com.example.gymlog.model.WorkoutDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val apiService: ApiService
) {

    // This exposes the LiveData list from the database.
    val allWorkouts: LiveData<List<Workout>> = workoutDao.getAllWorkouts()

    // Database Operations
    // These functions simply pass the request to the DAO. Local Database
    suspend fun insert(workout: Workout) {
        workoutDao.insert(workout)
    }

    suspend fun delete(workout: Workout) {
        workoutDao.delete(workout)
    }

    suspend fun deleteAll() {
        workoutDao.deleteAll()
    }

    suspend fun update(workout: Workout) {
        workoutDao.update(workout)
    }

    fun getWorkoutById(id: Int): LiveData<Workout> {
        return workoutDao.getWorkoutById(id)
    }

    fun getRecentWorkouts(sinceDate: Long): LiveData<List<Workout>> {
        return workoutDao.getRecentWorkouts(sinceDate)
    }

    fun getFilteredWorkouts(query: String, sinceDate: Long?): LiveData<List<Workout>> {
        return workoutDao.getFilteredWorkouts(query, sinceDate)
    }

    fun getWorkoutHistory(exerciseName: String): LiveData<List<Workout>> {
        return workoutDao.getWorkoutsHistory(exerciseName)
    }

    suspend fun getProfile() = workoutDao.getProfile()

    // API Operation
    // A clean sealed class to represent the result of an operation.
    sealed class ApiResult<out T> {
        data class Success<out T>(val data: T) : ApiResult<T>()
        data class Error(val message: String) : ApiResult<Nothing>()
    }


    //This function contains the business logic for fetching exercises from the API.
     //It handles success, failure, and network exceptions, returning a sealed result.

    suspend fun fetchFromApi(): ApiResult<List<ApiExercise>> {
        return try {
            // Make the API call
            val response = apiService.getExercises()

            if (response.isSuccessful) {
                val body = response.body()
                val results = body?.results
                if (results != null && results.isNotEmpty()) {
                    ApiResult.Success(results)
                } else {
                    ApiResult.Error("API Error: No exercises found.")
                }
            } else {
                ApiResult.Error("API Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network Error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
