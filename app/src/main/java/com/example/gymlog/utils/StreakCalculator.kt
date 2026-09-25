package com.example.gymlog.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object StreakCalculator {

    fun calculateStreak(sessionTimes: List<Long>, zoneId: ZoneId = ZoneId.systemDefault()): Int {
        if (sessionTimes.isEmpty()) return 0

        val uniqueDates = sessionTimes.map { millis ->
            Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
        }.distinct().sortedDescending()

        val today = LocalDate.now(zoneId)
        val mostRecent = uniqueDates.first()

        // If the most recent workout was before yesterday, streak is broken (0)
        if (mostRecent.isBefore(today.minusDays(1))) {
            return 0
        }

        var streak = 0
        var expectedDate = mostRecent

        for (date in uniqueDates) {
            if (date == expectedDate) {
                streak++
                expectedDate = expectedDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    fun calculateLongestStreak(sessionTimes: List<Long>, zoneId: ZoneId = ZoneId.systemDefault()): Int {
        if (sessionTimes.isEmpty()) return 0

        val uniqueDates = sessionTimes.map { millis ->
            Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
        }.distinct().sortedDescending()

        var maxStreak = 0
        var currentStreak = 0
        var expectedDate: LocalDate? = null

        for (date in uniqueDates) {
            if (expectedDate == null || date == expectedDate) {
                currentStreak++
                expectedDate = date.minusDays(1)
            } else {
                maxStreak = maxOf(maxStreak, currentStreak)
                currentStreak = 1
                expectedDate = date.minusDays(1)
            }
        }
        return maxOf(maxStreak, currentStreak)
    }
}
