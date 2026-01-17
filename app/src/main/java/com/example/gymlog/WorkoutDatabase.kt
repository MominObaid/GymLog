package com.example.gymlog

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Workout::class], version = 1, exportSchema = false)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao() : WorkoutDao

    companion object{
        @Volatile
        private var INSTANCE : WorkoutDatabase? = null

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
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}