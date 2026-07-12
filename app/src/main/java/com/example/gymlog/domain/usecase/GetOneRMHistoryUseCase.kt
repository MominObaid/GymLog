package com.example.gymlog.domain.usecase

import com.example.gymlog.RoutineRepository
import com.example.gymlog.model.OneRMPoint
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOneRMHistoryUseCase @Inject constructor(
    private val repository: RoutineRepository
) {
    operator fun invoke(profileId: Int, exerciseName: String): Flow<List<OneRMPoint>> {
        return repository.getOneRMHistory(profileId, exerciseName)
    }
}
