package com.example.gymlog.utils

import java.util.*
import java.util.concurrent.TimeUnit

object StreakCalculator {

    fun calculateStreak(sessionTimes: List<Long>): Int {
        if (sessionTimes.isEmpty()) return 0

        val uniqueDates = sessionTimes.map { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending()

        var streak = 0
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        val todayMillis = today.timeInMillis

        var currentCheckDate = todayMillis
        
        // If the most recent workout was not today or yesterday, streak is 0
        if (uniqueDates.first() < todayMillis - TimeUnit.DAYS.toMillis(1)) {
            return 0
        }

        // Adjust starting check date if the most recent workout was today or yesterday
        currentCheckDate = uniqueDates.first()

        for (date in uniqueDates) {
            if (date == currentCheckDate) {
                streak++
                currentCheckDate -= TimeUnit.DAYS.toMillis(1)
            } else {
                break
            }
        }

        return streak
    }

    fun calculateLongestStreak(sessionTimes: List<Long>): Int {
        if (sessionTimes.isEmpty()) return 0

        val uniqueDates = sessionTimes.map { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending()

        var maxStreak = 0
        var currentStreak = 0
        var lastDate: Long? = null

        for (date in uniqueDates) {
            if (lastDate == null || lastDate - date == TimeUnit.DAYS.toMillis(1)) {
                currentStreak++
            } else {
                maxStreak = maxOf(maxStreak, currentStreak)
                currentStreak = 1
            }
            lastDate = date
        }
        return maxOf(maxStreak, currentStreak)
    }
}
