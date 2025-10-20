package com.example.gymapplktrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val notes: String? = null
)
