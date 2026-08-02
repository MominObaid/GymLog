package com.example.gymlog.ui.session

import com.example.gymlog.model.WorkoutSessionEntity
import com.example.gymlog.model.WorkoutSetEntity

data class SessionUiState(
    val session: WorkoutSessionEntity? = null,
    val sets: List<WorkoutSetEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
