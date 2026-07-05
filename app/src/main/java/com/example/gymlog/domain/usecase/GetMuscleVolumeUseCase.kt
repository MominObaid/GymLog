package com.example.gymlog.domain.usecase

import com.example.gymlog.RoutineRepository
import com.example.gymlog.model.MuscleVolume
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMuscleVolumeUseCase @Inject constructor(
    private val repository: RoutineRepository
) {
    operator fun invoke(profileId: Int): Flow<List<MuscleVolume>> {
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        return repository.getMuscleGroupVolume(profileId, oneWeekAgo)
    }
}
