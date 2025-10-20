package com.example.gymapplktrack.domain

import com.example.gymapplktrack.domain.model.DailyLog
import com.example.gymapplktrack.domain.model.StreakMode
import com.example.gymapplktrack.domain.util.StreakCalculator
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCalculatorTest {
    @Test
    fun `day streak counts consecutive workouts`() {
        val today = LocalDate.of(2024, 1, 10)
        val logs = listOf(
            DailyLog(today, true, false),
            DailyLog(today.minusDays(1), true, false),
            DailyLog(today.minusDays(2), false, false)
        )

        val streak = StreakCalculator.calculate(logs, StreakMode.DIAS, today)

        assertEquals(2, streak)
    }

    @Test
    fun `week streak counts active weeks`() {
        val today = LocalDate.of(2024, 1, 20)
        val logs = listOf(
            DailyLog(LocalDate.of(2024, 1, 15), true, false),
            DailyLog(LocalDate.of(2024, 1, 9), true, false),
            DailyLog(LocalDate.of(2024, 1, 2), false, false)
        )

        val streak = StreakCalculator.calculate(logs, StreakMode.SEMANAS, today)

        assertEquals(2, streak)
    }
}
