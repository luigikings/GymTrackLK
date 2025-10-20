package com.example.gymapplktrack.domain

import com.example.gymapplktrack.domain.util.CalendarUtils
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarUtilsTest {
    @Test
    fun `markDays returns matching days`() {
        val month = YearMonth.of(2024, 2)
        val dates = listOf(
            LocalDate.of(2024, 2, 1),
            LocalDate.of(2024, 2, 10),
            LocalDate.of(2024, 3, 5)
        )

        val marks = CalendarUtils.markDays(dates, month)

        assertEquals(setOf(1, 10), marks)
    }
}
