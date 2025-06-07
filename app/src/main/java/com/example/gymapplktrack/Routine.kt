package com.example.gymapplktrack

/**
 * Represents a workout routine with a name and the list of exercise names
 * that belong to it.
 */
data class Routine(
    val name: String,
    val exercises: MutableList<String> = mutableListOf()
)
