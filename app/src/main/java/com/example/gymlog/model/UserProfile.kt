package com.example.gymlog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val age: Int = 0,
    val height: Float = 0f,
    val currentWeight: Float = 0f,
    val targetWeight: Float = 0f,
    val goal: String = "",
    val experienceLevel: String = "",
    val workoutDaysPerWeek: Int = 3,
    val availableEquipment: String = "",
    val injuries: String = "",
    val isActive: Boolean = false,
    val avatarColor: Int = 0xFF1976D2.toInt() // Default blue
)
