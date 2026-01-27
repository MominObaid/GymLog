package com.example.gymlog

import androidx.room.PrimaryKey
import androidx.room.Entity
@Entity(tableName = "workout_table")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String,
    var sets: Int,
    var reps: Int,
    var weight: Double,
    var date: String,
)