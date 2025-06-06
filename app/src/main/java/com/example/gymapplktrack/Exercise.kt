package com.example.gymapplktrack

import android.net.Uri

data class Exercise(
    val name: String,
    val record: String,
    val imageUri: Uri? = null
)
