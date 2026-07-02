package com.example.gymlog.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gymlog.RoutineRepository
import com.example.gymlog.utils.NotificationHelper
import com.example.gymlog.utils.StreakCalculator
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

class SmartReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SmartReminderEntryPoint {
        fun repository(): RoutineRepository
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, SmartReminderEntryPoint::class.java)
        val repository = entryPoint.repository()

        val profile = repository.getProfile() ?: return Result.success()
        val times = repository.getAllSessionTimes(profile.id)
        val currentStreak = StreakCalculator.calculateStreak(times)

        if (currentStreak > 0) {
            // Check if user has worked out today
            val today = System.currentTimeMillis()
            val lastWorkout = times.firstOrNull() ?: 0L
            val diff = today - lastWorkout
            
            // If it's been more than 24 hours but less than 48, streak is at risk
            if (diff > TimeUnit.HOURS.toMillis(24) && diff < TimeUnit.HOURS.toMillis(40)) {
                NotificationHelper.showNotification(
                    applicationContext,
                    NotificationHelper.WORKOUT_CHANNEL_ID,
                    "Streak at Risk! 🔥",
                    "Don't lose your $currentStreak-day streak. Time to hit the gym!",
                    2002
                )
            }
        }

        return Result.success()
    }
}
