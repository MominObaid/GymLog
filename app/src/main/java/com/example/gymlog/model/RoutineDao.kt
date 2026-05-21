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

    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

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

    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Int)

    @Query("DELETE FROM session_exercises WHERE sessionId = :sessionId")
    suspend fun deleteExercisesBySessionId(sessionId: Int)

    // Session Exercises
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionExercise(exercise: SessionExerciseEntity)

    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId")
    fun getExercisesForSession(sessionId: Int): Flow<List<SessionExerciseEntity>>

    // PR and Milestone Queries
    @Query("SELECT MAX(weight) FROM session_exercises WHERE exerciseName = :exerciseName")
    suspend fun getMaxWeightForExercise(exerciseName: String): Float?

    @Query("SELECT MAX(reps) FROM session_exercises WHERE exerciseName = :exerciseName")
    suspend fun getMaxRepsForExercise(exerciseName: String): Int?

    @Query("SELECT COUNT(*) FROM workout_sessions")
    suspend fun getWorkoutCount(): Int

    @Query("SELECT startTime FROM workout_sessions ORDER BY startTime DESC")
    suspend fun getAllSessionTimes(): List<Long>

    // Volume Analytics
    @Query("SELECT SUM(weight * reps) FROM session_exercises WHERE sessionId = :sessionId")
    suspend fun getSessionVolume(sessionId: Int): Float?

    @Query("""
        SELECT SUM(se.weight * se.reps) 
        FROM session_exercises se 
        JOIN workout_sessions ws ON se.sessionId = ws.id 
        WHERE ws.startTime >= :since
    """)
    suspend fun getTotalVolumeSince(since: Long): Float?

    @Query("""
        SELECT se.exerciseName, MAX(se.weight) as maxWeight 
        FROM session_exercises se 
        GROUP BY se.exerciseName 
        ORDER BY maxWeight DESC 
        LIMIT 5
    """)
    suspend fun getStrongestExercises(): List<ExerciseStat>

    @Query("""
        SELECT ws.startTime as date, SUM(se.weight * se.reps) as volume 
        FROM workout_sessions ws 
        JOIN session_exercises se ON ws.id = se.sessionId 
        GROUP BY ws.id 
        ORDER BY ws.startTime ASC
    """)
    fun getVolumeHistory(): Flow<List<VolumePoint>>

    @Query("""
        SELECT se.weight 
        FROM session_exercises se 
        JOIN workout_sessions ws ON se.sessionId = ws.id 
        WHERE se.exerciseName = :exerciseName 
        ORDER BY ws.startTime DESC
    """)
    suspend fun getWeightHistoryForExercise(exerciseName: String): List<Float>

    // User Profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE id = 0")
    suspend fun getProfile(): UserProfile?
}

data class ExerciseStat(val exerciseName: String, val maxWeight: Float)
data class VolumePoint(val date: Long, val volume: Float)
