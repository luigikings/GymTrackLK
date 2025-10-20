package com.example.gymapplktrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val imageUri: String? = null,
    val category: String? = null,
    val notes: String? = null
)
