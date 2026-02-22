package com.example.gymlog.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "workout_table")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @SerializedName("name")
    var name: String?,
    var sets: Int,
    var reps: Int,
    var weight: Double,
    var date: String,
)