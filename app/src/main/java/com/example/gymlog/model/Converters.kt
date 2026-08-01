package com.example.gymlog.model

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromWorkoutStatus(status: WorkoutStatus): String {
        return status.name
    }

    @TypeConverter
    fun toWorkoutStatus(status: String): WorkoutStatus {
        return WorkoutStatus.valueOf(status)
    }
}
