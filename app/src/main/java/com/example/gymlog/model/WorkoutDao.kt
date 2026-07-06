package com.example.gymlog.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {  //WorkoutDao is the Data Access Object
    @Insert
    suspend fun insert(workout: Workout)
    @Update
    suspend fun update(workout: Workout)
    @Delete
    suspend fun delete(workout : Workout)

    @Query("SELECT * FROM workout_table WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllWorkouts(profileId: Int): Flow<List<Workout>>

    @Query("SELECT * FROM workout_table WHERE profileId = :profileId AND date >= :sinceDate ORDER BY id DESC")
    fun getRecentWorkouts(profileId: Int, sinceDate: Long): Flow<List<Workout>>

    @Query("""
        SELECT * FROM workout_table 
        WHERE profileId = :profileId 
        AND (name LIKE '%' || :query || '%') 
        AND (:sinceDate IS NULL OR date >= :sinceDate) 
        ORDER BY date DESC, id DESC
    """)
    fun getFilteredWorkouts(profileId: Int, query: String, sinceDate: Long?): Flow<List<Workout>>

    @Query("DELETE FROM workout_table WHERE profileId = :profileId")
    suspend fun deleteAll(profileId: Int)

    @Query("SELECT * FROM workout_table WHERE id = :id")
    fun getWorkoutById(id: Int): Flow<Workout?>

    @Query("SELECT * FROM workout_table WHERE profileId = :profileId AND name = :exerciseName ORDER BY date ASC")
    fun getWorkoutsHistory(profileId: Int, exerciseName: String): Flow<List<Workout>>

    @Query("SELECT * FROM user_profile WHERE id = 0")
    suspend fun getProfile(): UserProfile?
}