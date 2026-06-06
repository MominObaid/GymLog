package com.example.gymlog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 0, // Only one profile
    val name: String = "",
    val age: Int = 0,
    val height: Float = 0f, // in cm
    val currentWeight: Float = 0f, // in kg
    val targetWeight: Float = 0f, // in kg
    val goal: String = "", // e.g., "Muscle Gain", "Fat Loss", "Strength"
    val experienceLevel: String = "", // e.g., "Beginner", "Intermediate", "Advanced"
    val workoutDaysPerWeek: Int = 3,
    val availableEquipment: String = "", // Comma separated list
    val injuries: String = "" // Comma separated list
)
