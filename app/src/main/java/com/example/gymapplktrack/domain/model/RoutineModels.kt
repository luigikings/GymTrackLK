package com.example.gymapplktrack.domain.model

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class RoutineOverview(
    val id: Long,
    val name: String,
    val createdAt: java.time.LocalDateTime,
    val exercises: List<RoutineExerciseItem>
)

data class RoutineExerciseItem(
    val id: Long,
    val exerciseId: Long,
    val name: String,
    val order: Int
)

data class WorkoutInProgress(
    val routineId: Long?,
    val startDate: LocalDate,
    val startTime: LocalTime,
    val exercises: List<WorkoutExercise>
)

data class WorkoutExercise(
    val exerciseId: Long,
    val name: String,
    val sets: List<WorkoutSet>
)

data class WorkoutSet(
    val weightKg: Float,
    val reps: Int
)

data class WorkoutSummary(
    val sessionId: Long,
    val duration: Duration,
    val totalExercises: Int,
    val totalSets: Int,
    val brokenRecords: List<PersonalRecordBreak>
)

data class PersonalRecordBreak(
    val exerciseName: String,
    val weightKg: Float,
    val reps: Int
)
