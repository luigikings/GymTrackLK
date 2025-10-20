package com.example.gymapplktrack.domain

import com.example.gymapplktrack.domain.util.PersonalRecordCalculator
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalRecordCalculatorTest {
    @Test
    fun `selectBest prioritizes weight and reps`() {
        val candidates = listOf(
            PersonalRecordCalculator.Candidate(80f, 5, LocalDate.now()),
            PersonalRecordCalculator.Candidate(82.5f, 3, LocalDate.now()),
            PersonalRecordCalculator.Candidate(82.5f, 4, LocalDate.now())
        )

        val best = PersonalRecordCalculator.selectBest(candidates)

        assertEquals(82.5f, best?.weightKg)
        assertEquals(4, best?.reps)
    }

    @Test
    fun `compare returns positive when first is better`() {
        val a = PersonalRecordCalculator.Candidate(100f, 4, LocalDate.now())
        val b = PersonalRecordCalculator.Candidate(95f, 8, LocalDate.now())

        assertTrue(PersonalRecordCalculator.compare(a, b) > 0)
    }
}
