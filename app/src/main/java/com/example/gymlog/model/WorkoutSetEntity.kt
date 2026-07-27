package com.example.gymlog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sets")
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sessionId: Int,
    val exerciseName: String,
    val muscleGroup: String = "Other",
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
