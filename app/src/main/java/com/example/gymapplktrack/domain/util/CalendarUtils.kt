package com.example.gymapplktrack.domain.util

import java.time.LocalDate
import java.time.YearMonth

object CalendarUtils {
    fun daysInMonth(month: YearMonth): List<LocalDate> {
        return (1..month.lengthOfMonth()).map { day -> month.atDay(day) }
    }

    fun markDays(dates: Collection<LocalDate>, month: YearMonth): Set<Int> {
        val set = dates.filter { it.year == month.year && it.month == month.month }
            .map { it.dayOfMonth }
            .toSet()
        return set
    }
}
