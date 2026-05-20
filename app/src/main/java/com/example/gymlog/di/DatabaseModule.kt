package com.example.gymlog.di

import android.content.Context
import androidx.room.Room
import com.example.gymlog.model.WorkoutDao
import com.example.gymlog.model.WorkoutDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WorkoutDatabase {
        return Room.databaseBuilder(
            context,
            WorkoutDatabase::class.java,
            "workout_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideWorkoutDao(database: WorkoutDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    fun provideRoutineDao(database: WorkoutDatabase): com.example.gymlog.model.RoutineDao {
        return database.routineDao()
    }
}
