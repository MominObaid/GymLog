package com.example.gymlog.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Workout::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        WorkoutSessionEntity::class,
        SessionExerciseEntity::class,
        UserProfile::class
    ],
    version = 10,
    exportSchema = false
)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao() : WorkoutDao
    abstract fun routineDao() : RoutineDao

    companion object{
        @Volatile
        private var INSTANCE : WorkoutDatabase? = null

        val MIGRATIONS = arrayOf<Migration>(
            // Future migration examples:
            // object : Migration(9, 10) { override fun migrate(db: SupportSQLiteDatabase) { ... } }
        )

        fun getDatabase(context: Context): WorkoutDatabase{
            val temInstance = INSTANCE
            if (temInstance != null ){
                return temInstance
            }

            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                ).addMigrations(*MIGRATIONS)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
