package com.example.gymlog.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymlog.SessionRepository
import com.example.gymlog.domain.usecase.FinishWorkoutUseCase
import com.example.gymlog.domain.usecase.SaveSetUseCase
import com.example.gymlog.domain.usecase.StartWorkoutUseCase
import com.example.gymlog.model.WorkoutSetEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val startWorkoutUseCase: StartWorkoutUseCase,
    private val finishWorkoutUseCase: FinishWorkoutUseCase,
    private val saveSetUseCase: SaveSetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private val _sessionFinished = MutableSharedFlow<String?>()
    val sessionFinished = _sessionFinished.asSharedFlow()

    init {
        observeActiveSession()
    }

    private fun observeActiveSession() {
        sessionRepository.getActiveSession()
            .onEach { session ->
                _uiState.update { it.copy(session = session) }
                if (session != null) {
                    observeSets(session.id)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSets(sessionId: Int) {
        sessionRepository.getSetsForSession(sessionId)
            .onEach { sets ->
                _uiState.update { it.copy(sets = sets) }
            }
            .launchIn(viewModelScope)
    }

    fun startWorkout(profileId: Int, routineId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                startWorkoutUseCase(profileId, routineId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addSet(exerciseName: String, muscleGroup: String, weight: Double, reps: Int) {
        val sessionId = uiState.value.session?.id ?: return
        val setNumber = (uiState.value.sets.filter { it.exerciseName == exerciseName }.maxOfOrNull { it.setNumber } ?: 0) + 1
        
        viewModelScope.launch {
            val newSet = WorkoutSetEntity(
                sessionId = sessionId,
                exerciseName = exerciseName,
                muscleGroup = muscleGroup,
                setNumber = setNumber,
                weight = weight,
                reps = reps,
                isCompleted = true
            )
            saveSetUseCase(newSet)
        }
    }

    fun updateSet(set: WorkoutSetEntity) {
        viewModelScope.launch {
            saveSetUseCase(set)
        }
    }

    fun deleteSet(set: WorkoutSetEntity) {
        viewModelScope.launch {
            sessionRepository.deleteSet(set)
        }
    }

    fun finishWorkout(notes: String? = null) {
        val sessionId = uiState.value.session?.id ?: return
        viewModelScope.launch {
            val prMessage = finishWorkoutUseCase(sessionId, notes)
            _sessionFinished.emit(prMessage)
        }
    }

    fun startRestTimer(durationMillis: Long) {
        val currentSession = uiState.value.session ?: return
        val endMillis = System.currentTimeMillis() + durationMillis
        viewModelScope.launch {
            sessionRepository.updateSession(currentSession.copy(restTimerEndMillis = endMillis))
        }
    }

    fun stopRestTimer() {
        val currentSession = uiState.value.session ?: return
        viewModelScope.launch {
            sessionRepository.updateSession(currentSession.copy(restTimerEndMillis = 0L))
        }
    }
}
