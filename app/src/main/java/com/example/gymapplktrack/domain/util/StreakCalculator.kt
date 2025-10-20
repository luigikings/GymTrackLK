package com.example.gymapplktrack.domain.util

import com.example.gymapplktrack.domain.model.DailyLog
import com.example.gymapplktrack.domain.model.StreakMode
import java.time.DayOfWeek
import java.time.LocalDate

object StreakCalculator {
    fun calculate(logs: List<DailyLog>, mode: StreakMode, today: LocalDate = LocalDate.now()): Int {
        val sorted = logs.sortedByDescending { it.date }
        return when (mode) {
            StreakMode.DIAS -> calculateDayStreak(sorted, today)
            StreakMode.SEMANAS -> calculateWeekStreak(sorted, today)
        }
    }

    private fun calculateDayStreak(logs: List<DailyLog>, today: LocalDate): Int {
        var streak = 0
        var cursor = today
        val logMap = logs.associateBy { it.date }
        while (true) {
            val log = logMap[cursor]
            if (log?.didWorkout == true) {
                streak += 1
                cursor = cursor.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateWeekStreak(logs: List<DailyLog>, today: LocalDate): Int {
        val grouped = logs.filter { it.didWorkout }.groupBy { weekOfYear(it.date) }
        var streak = 0
        var cursor = weekOfYear(today)
        while (true) {
            val weekEntries = grouped[cursor]
            if (weekEntries.isNullOrEmpty()) {
                break
            }
            streak += 1
            cursor = cursor.previous()
        }
        return streak
    }

    private fun weekOfYear(date: LocalDate): WeekKey {
        val monday = date.with(DayOfWeek.MONDAY)
        val year = monday.year
        val week = monday.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
        return WeekKey(year, week)
    }

    private data class WeekKey(val year: Int, val week: Int) {
        fun previous(): WeekKey {
            val field = java.time.temporal.WeekFields.ISO
            val monday = LocalDate.ofYearDay(year, 1).with(field.weekOfWeekBasedYear(), week.toLong()).with(DayOfWeek.MONDAY)
            val previous = monday.minusWeeks(1)
            val prevWeek = previous.get(field.weekOfWeekBasedYear())
            return WeekKey(previous.year, prevWeek)
        }
    }
}
