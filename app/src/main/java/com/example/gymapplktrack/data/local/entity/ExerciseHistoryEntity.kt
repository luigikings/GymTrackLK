package com.example.gymapplktrack.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "exercise_history",
    primaryKeys = ["exerciseId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("date")]
)
data class ExerciseHistoryEntity(
    val exerciseId: Long,
    val date: java.time.LocalDate
)
