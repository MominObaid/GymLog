package com.example.gymlog.domain.usecase

import com.example.gymlog.SessionRepository
import com.example.gymlog.model.WorkoutSetEntity
import javax.inject.Inject

class SaveSetUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(set: WorkoutSetEntity) {
        if (set.id == 0) {
            sessionRepository.insertSet(set)
        } else {
            sessionRepository.updateSet(set)
        }
    }
}
