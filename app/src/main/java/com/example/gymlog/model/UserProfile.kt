package com.example.gymlog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 0, // Only one profile
    val name: String = "",
    val goal: String = "", // e.g., "Muscle Gain", "Fat Loss", "Strength"
    val experienceLevel: String = "", // e.g., "Beginner", "Intermediate", "Advanced"
    val workoutDaysPerWeek: Int = 3,
    val availableEquipment: String = "", // Comma separated list
    val injuries: String = "" // Comma separated list
)
