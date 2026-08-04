package com.example.gymlog

import com.example.gymlog.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao
) {
    suspend fun startSession(profileId: Int, routineId: Int): Long {
        val session = WorkoutSessionEntity(
            profileId = profileId,
            routineId = routineId,
            startTime = System.currentTimeMillis(),
            status = WorkoutStatus.ACTIVE
        )
        return sessionDao.insertSession(session)
    }

    suspend fun updateSession(session: WorkoutSessionEntity) {
        sessionDao.updateSession(session)
    }

    fun getActiveSession(): Flow<WorkoutSessionEntity?> {
        return sessionDao.getActiveSession()
    }

    suspend fun getSessionById(sessionId: Int): WorkoutSessionEntity? {
        return sessionDao.getSessionById(sessionId)
    }

    fun getSetsForSession(sessionId: Int): Flow<List<WorkoutSetEntity>> {
        return sessionDao.getSetsForSession(sessionId)
    }

    suspend fun getSetsForSessionSync(sessionId: Int): List<WorkoutSetEntity> {
        return sessionDao.getSetsForSessionSync(sessionId)
    }

    suspend fun insertSet(set: WorkoutSetEntity) {
        sessionDao.insertSet(set)
    }

    suspend fun updateSet(set: WorkoutSetEntity) {
        sessionDao.updateSet(set)
    }

    suspend fun deleteSet(set: WorkoutSetEntity) {
        sessionDao.deleteSet(set)
    }
}
