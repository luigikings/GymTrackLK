package com.example.gymapplktrack.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "personal_records",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PersonalRecordEntity(
    @PrimaryKey val exerciseId: Long,
    val bestWeightKg: Float,
    val bestReps: Int,
    val firstAchievedDate: LocalDate
)
