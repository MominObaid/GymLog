//package com.example.gymlog
//class WorkoutRepository(
//    private val workoutDao: WorkoutDao,
//    private val apiService: ApiService,
//    private val workoutDatabase: WorkoutDatabase
//) {
//
//    //    private val workoutLiveData: MutableLiveData<Workout>
//    val allWorkouts: LiveData<List<Workout>> = workoutDao.getAllWorkouts()
////        get() = workoutLiveData
//
//    suspend fun insert(workout: Workout) {
//        workoutDao.insert(workout)
//    }
//
//    suspend fun delete(workout: Workout) {
//        workoutDao.delete(workout)
//    }
//
//    suspend fun deleteAll() {
//        workoutDao.deleteAll()
//    }
//
//    suspend fun update(workout: Workout) {
//        workoutDao.update(workout)
//    }
//
//
//    fun getWorkoutById(id: Int): LiveData<Workout> {
//        return workoutDao.getWorkoutById(id)
//    }
//
//    sealed class ApiResult<T> {
//        data class Success<T>(val data: T) : ApiResult<T>()
//        data class Error(val message: String) : ApiResult<Nothing>()
//    }

//    suspend fun fetchFromApi(): ApiResult<List<ApiExercise>> {
////        val response = RetrofitInstance.api.getExercises()
//        return when(apiService.Su) {
//
//        }
//                        if (response.isSuccessful) {
//                val body = response.body()
//                if (body != null && body.results.isNotEmpty()) {
//                    //Post the successful result to the LiveData
//                    ApiResult.Success(body.results)
//                } else {
//                    //Post an Error message
//                    ApiResult.Error("API Error: Response body is empty)}")
//                }
//            } else {
//                apiError.postValue("API Error ${response.code()} - ${response.message()}")
//            }
//        } catch (e: Exception) {
//            //Post an error message for network exception
//            apiError.postValue("Network Error: ${e.message}")
////        }
////    }


//    suspend fun fetchFromApi(): ApiResult<List<ApiExercise>> {
//        return try {
//            val response = apiService.getExercises()
//            if (response.isSuccessful != null) {
//                ApiResult.Success(response.)
//            } else {
//                ApiResult.Error(response.message())
//            }
//        } catch (e: Exception) {
//            ApiResult.Error("")
//        }
//    }
//}




package com.example.gymlog

import androidx.lifecycle.LiveData
import com.example.gymlog.api.ApiExercise
import com.example.gymlog.api.ApiService
import com.example.gymlog.model.Workout
import com.example.gymlog.model.WorkoutDao

// 1. constructor. Only request dependencies you actually use.
class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val apiService: ApiService
) {

    // This exposes the LiveData list from the database.
    val allWorkouts: LiveData<List<Workout>> = workoutDao.getAllWorkouts()

    // --- Database Operations ---
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

    fun getWorkoutHistory(exerciseName: String): LiveData<List<Workout>> {
        return workoutDao.getWorkoutsHistory(exerciseName)
    }


    // --- API Operation ---

    // 2. A clean sealed class to represent the result of an operation.
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
                // Check that the body is not null AND the results list is not empty
                if (body != null && body.results.isNotEmpty()) {
                    // On success, return the list of exercises
                    ApiResult.Success(body.results)
                } else {
                    // Handle the case of a successful response with an empty body
                    ApiResult.Error("API Error: Response body is empty.")
                }
            } else {
                // Handle non-successful HTTP responses (e.g., 404, 500)
                ApiResult.Error("API Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            // Handle network errors (e.g., no internet connection) or other exceptions
            ApiResult.Error("Network Error: ${e.message}")
        }
    }
}
