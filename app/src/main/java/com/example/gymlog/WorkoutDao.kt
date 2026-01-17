package com.example.gymlog

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.*

@Dao
interface WorkoutDao {  //WorkoutDao is the Data Access Object
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(workout: Workout)

    @Delete
    suspend fun delete(workout : Workout)

    @Query("SELECT * FROM workout_table ORDER BY id DESC")
    fun getAllWorkouts(): LiveData<List<Workout>>

    @Query("DELETE FROM workout_table")
    suspend fun deleteAll()

}