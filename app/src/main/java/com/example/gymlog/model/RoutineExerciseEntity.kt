package com.example.gymlog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_exercises")
data class RoutineExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val routineId: Int,
    val exerciseName: String,
    val muscleGroup: String = "Other", // Added for Advanced Analytics
    val targetSets: Int,
    val targetReps: Int,
    val exerciseOrder: Int
)
