package com.example.gymlog

import androidx.room.PrimaryKey
import androidx.room.Entity
@Entity(tableName = "workout_table")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val date: String,
)