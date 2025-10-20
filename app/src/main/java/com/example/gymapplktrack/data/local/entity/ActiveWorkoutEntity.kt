package com.example.gymapplktrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "active_workout")
data class ActiveWorkoutEntity(
    @PrimaryKey val id: Int = 0,
    val routineId: Long?,
    val startDate: LocalDate,
    val startTime: LocalTime,
    val payload: ActiveWorkoutPayload
)

data class ActiveWorkoutPayload(
    val exercises: List<ActiveWorkoutExercise>
)

data class ActiveWorkoutExercise(
    val exerciseId: Long,
    val name: String,
    val sets: List<ActiveWorkoutSet>
)

data class ActiveWorkoutSet(
    val weightKg: Float,
    val reps: Int
)
