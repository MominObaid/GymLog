package com.example.gymlog.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

    @Query("SELECT * FROM routines WHERE profileId = :profileId ORDER BY createdAt DESC")
    fun getAllRoutines(profileId: Int): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: Int): RoutineEntity?

    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutineById(routineId: Int)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteExercisesByRoutineId(routineId: Int)

    // Routine Exercises
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercise(exercise: RoutineExerciseEntity)

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY exerciseOrder ASC")
    fun getExercisesForRoutine(routineId: Int): Flow<List<RoutineExerciseEntity>>

    @Transaction
    suspend fun insertRoutineWithExercises(routine: RoutineEntity, exercises: List<RoutineExerciseEntity>) {
        val routineId = insertRoutine(routine).toInt()
        exercises.forEach { exercise ->
            insertRoutineExercise(exercise.copy(id = 0, routineId = routineId))
        }
    }

    @Transaction
    suspend fun updateRoutineWithExercises(routine: RoutineEntity, exercises: List<RoutineExerciseEntity>) {
        updateRoutine(routine)
        deleteExercisesByRoutineId(routine.id)
        exercises.forEach { exercise ->
            insertRoutineExercise(exercise.copy(id = 0, routineId = routine.id))
        }
    }

    // Workout Sessions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Query("SELECT * FROM workout_sessions WHERE profileId = :profileId ORDER BY startTime DESC")
    fun getAllSessions(profileId: Int): Flow<List<WorkoutSessionEntity>>

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Int)

    @Query("DELETE FROM session_exercises WHERE sessionId = :sessionId")
    suspend fun deleteExercisesBySessionId(sessionId: Int)

    @Query("SELECT * FROM workout_sessions WHERE profileId = :profileId ORDER BY startTime DESC LIMIT 3")
    fun getRecentSessions(profileId: Int): Flow<List<WorkoutSessionEntity>>

    @Query("""
        SELECT se.exerciseName 
        FROM session_exercises se
        JOIN workout_sessions ws ON se.sessionId = ws.id
        WHERE ws.profileId = :profileId
        GROUP BY se.exerciseName 
        ORDER BY COUNT(*) DESC 
        LIMIT 1
    """)
    suspend fun getFavoriteExercise(profileId: Int): String?

    // Session Exercises
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionExercise(exercise: SessionExerciseEntity)

    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId")
    fun getExercisesForSession(sessionId: Int): Flow<List<SessionExerciseEntity>>

    // PR and Milestone Queries
    @Query("""
        SELECT MAX(se.weight) 
        FROM session_exercises se
        JOIN workout_sessions ws ON se.sessionId = ws.id
        WHERE ws.profileId = :profileId AND se.exerciseName = :exerciseName
    """)
    suspend fun getMaxWeightForExercise(profileId: Int, exerciseName: String): Float?

    @Query("""
        SELECT MAX(se.reps) 
        FROM session_exercises se
        JOIN workout_sessions ws ON se.sessionId = ws.id
        WHERE ws.profileId = :profileId AND se.exerciseName = :exerciseName
    """)
    suspend fun getMaxRepsForExercise(profileId: Int, exerciseName: String): Int?

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE profileId = :profileId")
    suspend fun getWorkoutCount(profileId: Int): Int

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE profileId = :profileId AND startTime >= :since")
    suspend fun getWorkoutCountSince(profileId: Int, since: Long): Int

    @Query("SELECT startTime FROM workout_sessions WHERE profileId = :profileId ORDER BY startTime DESC")
    suspend fun getAllSessionTimes(profileId: Int): List<Long>

    // Volume Analytics
    @Query("SELECT SUM(weight * reps) FROM session_exercises WHERE sessionId = :sessionId")
    suspend fun getSessionVolume(sessionId: Int): Float?

    @Query("""
        SELECT SUM(se.weight * se.reps) 
        FROM session_exercises se 
        JOIN workout_sessions ws ON se.sessionId = ws.id 
        WHERE ws.profileId = :profileId AND ws.startTime >= :since
    """)
    suspend fun getTotalVolumeSince(profileId: Int, since: Long): Float?

    @Query("""
        SELECT se.exerciseName, COALESCE(MAX(se.weight), 0.0) as maxWeight 
        FROM session_exercises se 
        JOIN workout_sessions ws ON se.sessionId = ws.id
        WHERE ws.profileId = :profileId
        GROUP BY se.exerciseName 
        ORDER BY maxWeight DESC 
        LIMIT 5
    """)
    suspend fun getStrongestExercises(profileId: Int): List<ExerciseStat>

    @Query("""
        SELECT ws.startTime as date, COALESCE(SUM(se.weight * se.reps), 0.0) as volume
        FROM workout_sessions ws 
        JOIN session_exercises se ON ws.id = se.sessionId 
        WHERE ws.profileId = :profileId
        GROUP BY ws.id 
        ORDER BY ws.startTime ASC
    """)
    fun getVolumeHistory(profileId: Int): Flow<List<VolumePoint>>

    @Query("""
        SELECT se.weight 
        FROM session_exercises se 
        JOIN workout_sessions ws ON se.sessionId = ws.id 
        WHERE ws.profileId = :profileId AND se.exerciseName = :exerciseName 
        ORDER BY ws.startTime DESC
    """)
    suspend fun getWeightHistoryForExercise(profileId: Int, exerciseName: String): List<Float>

    @Query("""
        SELECT se.muscleGroup as label, COUNT(*) as value
        FROM session_exercises se
        JOIN workout_sessions ws ON se.sessionId = ws.id
        WHERE ws.profileId = :profileId AND ws.startTime >= :since
        GROUP BY se.muscleGroup
    """)
    fun getMuscleGroupVolume(profileId: Int, since: Long): Flow<List<MuscleVolume>>

    @Query("""
        SELECT ws.startTime as date, MAX(se.weight * (1 + se.reps/30.0)) as oneRM
        FROM session_exercises se
        JOIN workout_sessions ws ON se.sessionId = ws.id
        WHERE ws.profileId = :profileId AND se.exerciseName = :exerciseName
        GROUP BY ws.id
        ORDER BY ws.startTime ASC
    """)
    fun getOneRMHistory(profileId: Int, exerciseName: String): Flow<List<OneRMPoint>>

    // User Profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile): Long

    @Query("SELECT * FROM user_profile WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProfile(): UserProfile?

    @Query("SELECT * FROM user_profile")
    fun getAllProfiles(): Flow<List<UserProfile>>

    @Query("UPDATE user_profile SET isActive = 0")
    suspend fun deactivateAllProfiles()

    @Query("UPDATE user_profile SET isActive = 1 WHERE id = :profileId")
    suspend fun activateProfile(profileId: Int)

    @Transaction
    suspend fun setActiveProfile(profileId: Int) {
        deactivateAllProfiles()
        activateProfile(profileId)
    }

    @Delete
    suspend fun deleteProfile(profile: UserProfile)
}

data class ExerciseStat(val exerciseName: String, val maxWeight: Float)
data class VolumePoint(val date: Long, val volume: Float)
data class MuscleVolume(val label: String, val value: Int)
data class OneRMPoint(val date: Long, val oneRM: Double)
