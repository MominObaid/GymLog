package com.example.gymlog

import com.example.gymlog.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao
) {
    val allRoutines: Flow<List<RoutineEntity>> = routineDao.getAllRoutines()

    suspend fun insertRoutine(routine: RoutineEntity): Long {
        return routineDao.insertRoutine(routine)
    }

    suspend fun insertRoutineWithExercises(routine: RoutineEntity, exercises: List<RoutineExerciseEntity>) {
        routineDao.insertRoutineWithExercises(routine, exercises)
    }

    suspend fun updateRoutineWithExercises(routine: RoutineEntity, exercises: List<RoutineExerciseEntity>) {
        routineDao.updateRoutineWithExercises(routine, exercises)
    }

    suspend fun getRoutineById(id: Int): RoutineEntity? {
        return routineDao.getRoutineById(id)
    }

    suspend fun insertRoutineExercise(exercise: RoutineExerciseEntity) {
        routineDao.insertRoutineExercise(exercise)
    }

    suspend fun deleteRoutine(routineId: Int) {
        routineDao.deleteRoutineById(routineId)
        routineDao.deleteExercisesByRoutineId(routineId)
    }

    fun getExercisesForRoutine(routineId: Int): Flow<List<RoutineExerciseEntity>> {
        return routineDao.getExercisesForRoutine(routineId)
    }

    suspend fun insertSession(session: WorkoutSessionEntity): Long {
        return routineDao.insertSession(session)
    }

    suspend fun insertSessionExercise(exercise: SessionExerciseEntity) {
        routineDao.insertSessionExercise(exercise)
    }

    suspend fun deleteSession(sessionId: Int) {
        routineDao.deleteSessionById(sessionId)
        routineDao.deleteExercisesBySessionId(sessionId)
    }

    fun getAllSessions(): Flow<List<WorkoutSessionEntity>> {
        return routineDao.getAllSessions()
    }

    fun getRecentSessions(): Flow<List<WorkoutSessionEntity>> {
        return routineDao.getRecentSessions()
    }

    suspend fun getFavoriteExercise(): String? {
        return routineDao.getFavoriteExercise()
    }

    suspend fun getMaxWeightForExercise(exerciseName: String): Float? {
        return routineDao.getMaxWeightForExercise(exerciseName)
    }

    suspend fun getMaxRepsForExercise(exerciseName: String): Int? {
        return routineDao.getMaxRepsForExercise(exerciseName)
    }

    suspend fun getWorkoutCount(): Int {
        return routineDao.getWorkoutCount()
    }

    suspend fun getWorkoutCountSince(since: Long): Int {
        return routineDao.getWorkoutCountSince(since)
    }

    suspend fun getAllSessionTimes(): List<Long> {
        return routineDao.getAllSessionTimes()
    }

    suspend fun getSessionVolume(sessionId: Int): Float? {
        return routineDao.getSessionVolume(sessionId)
    }

    suspend fun getTotalVolumeSince(since: Long): Float? {
        return routineDao.getTotalVolumeSince(since)
    }

    suspend fun getStrongestExercises(): List<ExerciseStat> {
        return routineDao.getStrongestExercises()
    }

    fun getVolumeHistory(): Flow<List<VolumePoint>> {
        return routineDao.getVolumeHistory()
    }

    suspend fun getWeightHistoryForExercise(exerciseName: String): List<Float> {
        return routineDao.getWeightHistoryForExercise(exerciseName)
    }

    suspend fun insertProfile(profile: UserProfile) {
        routineDao.insertProfile(profile)
    }

    suspend fun getProfile(): UserProfile? {
        return routineDao.getProfile()
    }
}
