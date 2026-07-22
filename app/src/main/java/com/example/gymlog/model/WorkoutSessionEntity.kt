package com.example.gymlog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val profileId: Int,
    val routineId: Int,
    val startTime: Long,
    val endTime: Long = 0L,
    val status: WorkoutStatus = WorkoutStatus.NOT_STARTED,
    val durationMillis: Long = 0L,
    val totalVolume: Double = 0.0,
    val restTimerEndMillis: Long = 0L,
    val notes: String? = null
)
