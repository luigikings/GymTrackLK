package com.example.gymapplktrack.domain.util

import java.time.LocalDate

object PersonalRecordCalculator {
    data class Candidate(
        val weightKg: Float,
        val reps: Int,
        val date: LocalDate
    )

    fun selectBest(candidates: List<Candidate>): Candidate? {
        return candidates.maxWithOrNull { a, b -> compare(a, b) }
    }

    fun compare(a: Candidate, b: Candidate): Int {
        return when {
            a.weightKg > b.weightKg -> 1
            a.weightKg < b.weightKg -> -1
            a.reps > b.reps -> 1
            a.reps < b.reps -> -1
            else -> 0
        }
    }
}
