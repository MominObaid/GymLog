package com.example.gymlog.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.gymlog.MainActivity
import com.example.gymlog.R
import com.example.gymlog.RoutineRepository
import com.example.gymlog.utils.StreakCalculator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var repository: RoutineRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.workout_widget)

        scope.launch {
            val activeProfile = repository.getProfile()
            if (activeProfile != null) {
                val times = repository.getAllSessionTimes(activeProfile.id)
                val streak = StreakCalculator.calculateStreak(times)
                views.setTextViewText(R.id.widgetStreakText, "$streak Day Streak")

                val routines = repository.getAllRoutines(activeProfile.id).first()
                if (routines.isNotEmpty()) {
                    views.setTextViewText(R.id.widgetTodayWorkout, "Next: ${routines.first().name}")
                }
            }

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetBtnStart, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        job.cancel()
    }
}
