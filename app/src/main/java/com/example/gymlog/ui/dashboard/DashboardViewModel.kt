package com.example.gymlog.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val profile = repository.getProfile()
            if (profile != null) {
                val greeting = repository.getGreeting()
                val streak = repository.getStreak(profile.id)
                val todayPlan = repository.getTodayPlan(profile.id)
                val weeklyGoal = repository.getWeeklyGoalStatus(profile.id)
                val recentWorkout = repository.getRecentWorkout(profile.id)
                val recoveryScore = repository.getSleepScore()

                _uiState.update {
                    it.copy(
                        greeting = greeting,
                        userName = profile.name,
                        streak = streak,
                        todayPlan = todayPlan,
                        weeklyGoal = weeklyGoal,
                        recentWorkout = recentWorkout,
                        recoveryScore = recoveryScore,
                        isLoading = false,
                        coachInsight = "Recovery looks good. Keep it up!" // Placeholder for Step 10
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
