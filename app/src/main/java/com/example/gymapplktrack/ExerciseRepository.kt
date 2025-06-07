package com.example.gymapplktrack

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ExerciseRepository(context: Context) {
    private val prefs = context.getSharedPreferences("exercises", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadExercises(): MutableList<Exercise> {
        val json = prefs.getString("list", null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<List<ExerciseDto>>() {}.type
            val dtos: List<ExerciseDto> = gson.fromJson(json, type)
            dtos.map { dto ->
                Exercise(
                    dto.name,
                    dto.records.map { ExerciseRecord(it.weight, it.reps, it.date) }.toMutableList(),
                    dto.imageUri?.let { uri -> Uri.parse(uri) }
                )
            }.toMutableList()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun saveExercises(exercises: List<Exercise>) {
        val dtos = exercises.map { ex ->
            ExerciseDto(
                ex.name,
                ex.records.map { ExerciseRecordDto(it.weight, it.reps, it.date) },
                ex.imageUri?.toString()
            )
        }
        prefs.edit().putString("list", gson.toJson(dtos)).apply()
    }

    private data class ExerciseRecordDto(val weight: Int, val reps: Int, val date: String)
    private data class ExerciseDto(val name: String, val records: List<ExerciseRecordDto>, val imageUri: String?)
}

