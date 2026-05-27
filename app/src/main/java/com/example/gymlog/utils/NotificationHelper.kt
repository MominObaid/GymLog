package com.example.gymlog.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gymlog.R

object NotificationHelper {

    const val WORKOUT_CHANNEL_ID = "workout_reminders"
    const val REST_TIMER_CHANNEL_ID = "rest_timer"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val workoutChannel = NotificationChannel(
                WORKOUT_CHANNEL_ID,
                "Workout Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to stay consistent with your workouts"
            }

            val restTimerChannel = NotificationChannel(
                REST_TIMER_CHANNEL_ID,
                "Rest Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for finished rest periods"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(workoutChannel)
            notificationManager.createNotificationChannel(restTimerChannel)
        }
    }

    fun showNotification(context: Context, channelId: String, title: String, message: String, notificationId: Int) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_add_24) // Replace with proper icon later
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Handle missing permission on Android 13+
            }
        }
    }
}
