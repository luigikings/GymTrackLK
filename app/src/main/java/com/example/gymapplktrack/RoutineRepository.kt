package com.example.gymapplktrack

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Simple persistence helper that stores routines in SharedPreferences using JSON.
 */
class RoutineRepository(context: Context) {
    private val prefs = context.getSharedPreferences("routines", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadRoutines(): MutableList<Routine> {
        val json = prefs.getString("list", null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<List<RoutineDto>>() {}.type
            val dtos: List<RoutineDto> = gson.fromJson(json, type)
            dtos.map { Routine(it.name, it.exercises.toMutableList()) }.toMutableList()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun saveRoutines(routines: List<Routine>) {
        val dtos = routines.map { RoutineDto(it.name, it.exercises) }
        prefs.edit().putString("list", gson.toJson(dtos)).apply()
    }

    private data class RoutineDto(val name: String, val exercises: List<String>)
}
