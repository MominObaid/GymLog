package com.example.gymlog.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity(tableName = "workout_table")
@JsonClass(generateAdapter = true)
data class Workout(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @Json(name = "name")
    var name: String?,
    var sets: Int,
    var reps: Int,
    var weight: Double,
    var date: Long,
)