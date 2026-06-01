package com.example.gymlog.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.gymlog.utils.NotificationHelper

class WorkoutReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): androidx.work.ListenableWorker.Result {
        NotificationHelper.showNotification(
            applicationContext,
            NotificationHelper.WORKOUT_CHANNEL_ID,
            "Time to Train!",
            "Don't forget your scheduled workout today 💪",
            2001
        )
        return androidx.work.ListenableWorker.Result.success()
    }
}
