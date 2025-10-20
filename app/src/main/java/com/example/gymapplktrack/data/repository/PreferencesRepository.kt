package com.example.gymapplktrack.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.gymapplktrack.domain.model.StreakMode
import com.example.gymapplktrack.domain.model.ThemePreference
import com.example.gymapplktrack.domain.model.UserPreferences
import com.example.gymapplktrack.domain.model.WeightUnit
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gym_preferences")

class PreferencesRepository(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme")
    private val unitKey = stringPreferencesKey("unit")
    private val streakModeKey = stringPreferencesKey("streak_mode")
    private val reminderEnabledKey = booleanPreferencesKey("creatine_enabled")
    private val reminderHourKey = intPreferencesKey("creatine_hour")
    private val reminderMinuteKey = intPreferencesKey("creatine_minute")

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            theme = prefs[themeKey]?.let { ThemePreference.valueOf(it) } ?: ThemePreference.SISTEMA,
            weightUnit = prefs[unitKey]?.let { WeightUnit.valueOf(it) } ?: WeightUnit.KG,
            streakMode = prefs[streakModeKey]?.let { StreakMode.valueOf(it) } ?: StreakMode.DIAS,
            creatineReminderEnabled = prefs[reminderEnabledKey] ?: false,
            creatineReminderTime = prefs[reminderHourKey]?.let { hour ->
                val minute = prefs[reminderMinuteKey] ?: 0
                LocalTime.of(hour, minute)
            }
        )
    }

    suspend fun updateTheme(theme: ThemePreference) {
        context.dataStore.edit { prefs ->
            prefs[themeKey] = theme.name
        }
    }

    suspend fun updateUnit(unit: WeightUnit) {
        context.dataStore.edit { prefs ->
            prefs[unitKey] = unit.name
        }
    }

    suspend fun updateStreakMode(mode: StreakMode) {
        context.dataStore.edit { prefs ->
            prefs[streakModeKey] = mode.name
        }
    }

    suspend fun updateCreatineReminder(enabled: Boolean, time: LocalTime?) {
        context.dataStore.edit { prefs ->
            prefs[reminderEnabledKey] = enabled
            if (time != null) {
                prefs[reminderHourKey] = time.hour
                prefs[reminderMinuteKey] = time.minute
            }
        }
    }
}
