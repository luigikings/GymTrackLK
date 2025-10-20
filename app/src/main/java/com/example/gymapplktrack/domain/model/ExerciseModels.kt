package com.example.gymapplktrack.domain.model

data class ExerciseOverview(
    val id: Long,
    val name: String,
    val imageUri: String?,
    val category: String?,
    val notes: String?,
    val personalRecord: PersonalRecord?,
    val usageCount: Int
)

data class PersonalRecord(
    val bestWeightKg: Float,
    val bestReps: Int,
    val firstAchievedDate: java.time.LocalDate
)

data class ExerciseDetail(
    val overview: ExerciseOverview,
    val history: List<java.time.LocalDate>,
    val sets: List<ExerciseSetHistory>
)

data class ExerciseSetHistory(
    val sessionId: Long,
    val date: java.time.LocalDate,
    val startTime: java.time.LocalTime,
    val endTime: java.time.LocalTime,
    val setIndex: Int,
    val weightKg: Float,
    val reps: Int,
    val notes: String?
)

data class ExerciseHistoryHighlight(
    val date: java.time.LocalDate
)

enum class ExerciseSort { ALPHABETICAL, MOST_USED, RECORD_FIRST }
