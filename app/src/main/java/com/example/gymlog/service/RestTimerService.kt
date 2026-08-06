package com.example.gymlog.service

import android.app.*
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.gymlog.MainActivity
import com.example.gymlog.R
import com.example.gymlog.utils.NotificationHelper

class RestTimerService : Service() {

    private var countDownTimer: CountDownTimer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val durationMillis = intent?.getLongExtra(EXTRA_DURATION_MILLIS, 0L) ?: 0L
        
        if (durationMillis > 0) {
            startForegroundService(durationMillis)
        } else {
            stopSelf()
        }
        
        return START_NOT_STICKY
    }

    private fun startForegroundService(durationMillis: Long) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, NotificationHelper.REST_TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.outline_trophy_24) // Using existing icon
            .setContentTitle("Resting...")
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notificationBuilder.build(),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notificationBuilder.build())
        }

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val display = String.format(java.util.Locale.getDefault(), "Rest: %02d:%02d", seconds / 60, seconds % 60)
                
                notificationBuilder.setContentText(display)
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                
                sendBroadcast(Intent(ACTION_TIMER_TICK).apply {
                    putExtra(EXTRA_MILLIS_LEFT, millisUntilFinished)
                    setPackage(packageName)
                })
            }

            override fun onFinish() {
                notificationBuilder.setContentText("Rest Over!")
                    .setOngoing(false)
                    .setAutoCancel(true)
                
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                
                NotificationHelper.showNotification(
                    this@RestTimerService,
                    NotificationHelper.REST_TIMER_CHANNEL_ID,
                    "Rest Finished",
                    "Time for your next set!",
                    1001
                )
                
                sendBroadcast(Intent(ACTION_TIMER_FINISHED).apply {
                    setPackage(packageName)
                })
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf()
            }
        }.start()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1002
        const val EXTRA_DURATION_MILLIS = "extra_duration_millis"
        const val ACTION_TIMER_TICK = "com.example.gymlog.TIMER_TICK"
        const val ACTION_TIMER_FINISHED = "com.example.gymlog.TIMER_FINISHED"
        const val EXTRA_MILLIS_LEFT = "extra_millis_left"
    }
}
