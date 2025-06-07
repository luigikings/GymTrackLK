package com.example.gymapplktrack

import android.net.Uri

data class ExerciseRecord(
    val weight: Int,
    val reps: Int,
    val date: String
)

data class Exercise(
    val name: String,
    val records: MutableList<ExerciseRecord> = mutableListOf(),
    val imageUri: Uri? = null
)
