package com.example.gymapplktrack.domain.model

import java.time.LocalDate

data class DailyLog(
    val date: LocalDate,
    val didWorkout: Boolean,
    val tookCreatine: Boolean
)

data class StreakInfo(
    val currentStreak: Int,
    val mode: StreakMode
)

enum class StreakMode { DIAS, SEMANAS }

data class ActivitySummary(
    val monthlySessions: Int,
    val topExercises: List<String>,
    val recentPersonalRecords: List<String>
)

data class UserPreferences(
    val theme: ThemePreference,
    val weightUnit: WeightUnit,
    val streakMode: StreakMode,
    val creatineReminderEnabled: Boolean,
    val creatineReminderTime: java.time.LocalTime?
)

enum class ThemePreference { CLARO, OSCURO, SISTEMA }

enum class WeightUnit { KG, LB }
