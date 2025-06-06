package com.example.gymapplktrack

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ExerciseRepository(context: Context) {
    private val prefs = context.getSharedPreferences("exercises", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadExercises(): MutableList<Exercise> {
        val json = prefs.getString("list", null)
        return if (json != null) {
            val type = object : TypeToken<List<ExerciseDto>>() {}.type
            val dtos: List<ExerciseDto> = gson.fromJson(json, type)
            dtos.map { Exercise(it.name, it.record, it.imageUri?.let { uri -> Uri.parse(uri) }) }.toMutableList()
        } else {
            mutableListOf()
        }
    }

    fun saveExercises(exercises: List<Exercise>) {
        val dtos = exercises.map { ExerciseDto(it.name, it.record, it.imageUri?.toString()) }
        prefs.edit().putString("list", gson.toJson(dtos)).apply()
    }

    private data class ExerciseDto(val name: String, val record: String, val imageUri: String?)
}

