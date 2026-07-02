package com.example.gymlog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_exercises")
data class SessionExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sessionId: Int,
    val exerciseName: String,
    val muscleGroup: String = "Other", // Added for Advanced Analytics
    val setNumber: Int,
    val reps: Int,
    val weight: Float
)
