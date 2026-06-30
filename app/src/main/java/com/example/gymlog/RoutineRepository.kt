package com.example.gymlog

import com.example.gymlog.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao
) {
    fun getAllRoutines(profileId: Int): Flow<List<RoutineEntity>> {
        return routineDao.getAllRoutines(profileId)
    }

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

    fun getAllSessions(profileId: Int): Flow<List<WorkoutSessionEntity>> {
        return routineDao.getAllSessions(profileId)
    }

    fun getRecentSessions(profileId: Int): Flow<List<WorkoutSessionEntity>> {
        return routineDao.getRecentSessions(profileId)
    }

    suspend fun getFavoriteExercise(profileId: Int): String? {
        return routineDao.getFavoriteExercise(profileId)
    }

    suspend fun getMaxWeightForExercise(profileId: Int, exerciseName: String): Float? {
        return routineDao.getMaxWeightForExercise(profileId, exerciseName)
    }

    suspend fun getMaxRepsForExercise(profileId: Int, exerciseName: String): Int? {
        return routineDao.getMaxRepsForExercise(profileId, exerciseName)
    }

    suspend fun getWorkoutCount(profileId: Int): Int {
        return routineDao.getWorkoutCount(profileId)
    }

    suspend fun getWorkoutCountSince(profileId: Int, since: Long): Int {
        return routineDao.getWorkoutCountSince(profileId, since)
    }

    suspend fun getAllSessionTimes(profileId: Int): List<Long> {
        return routineDao.getAllSessionTimes(profileId)
    }

    suspend fun getSessionVolume(sessionId: Int): Float? {
        return routineDao.getSessionVolume(sessionId)
    }

    suspend fun getTotalVolumeSince(profileId: Int, since: Long): Float? {
        return routineDao.getTotalVolumeSince(profileId, since)
    }

    suspend fun getStrongestExercises(profileId: Int): List<ExerciseStat> {
        return routineDao.getStrongestExercises(profileId)
    }

    fun getVolumeHistory(profileId: Int): Flow<List<VolumePoint>> {
        return routineDao.getVolumeHistory(profileId)
    }

    suspend fun getWeightHistoryForExercise(profileId: Int, exerciseName: String): List<Float> {
        return routineDao.getWeightHistoryForExercise(profileId, exerciseName)
    }

    fun getMuscleGroupVolume(profileId: Int, since: Long): Flow<List<MuscleVolume>> {
        return routineDao.getMuscleGroupVolume(profileId, since)
    }

    fun getOneRMHistory(profileId: Int, exerciseName: String): Flow<List<OneRMPoint>> {
        return routineDao.getOneRMHistory(profileId, exerciseName)
    }

    suspend fun insertProfile(profile: UserProfile) {
        routineDao.insertProfile(profile)
    }

    suspend fun getProfile(): UserProfile? {
        return routineDao.getActiveProfile()
    }

    fun getAllProfiles() = routineDao.getAllProfiles()

    suspend fun setActiveProfile(profileId: Int) {
        routineDao.setActiveProfile(profileId)
    }

    suspend fun deleteProfile(profile: UserProfile) {
        routineDao.deleteProfile(profile)
    }
}
