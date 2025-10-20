package com.example.gymapplktrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey val date: LocalDate,
    val didWorkout: Boolean,
    val tookCreatine: Boolean
)
