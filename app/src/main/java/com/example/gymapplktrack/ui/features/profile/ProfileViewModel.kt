package com.example.gymapplktrack.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gymapplktrack.AppContainer
import com.example.gymapplktrack.data.repository.GymRepository
import com.example.gymapplktrack.data.repository.PreferencesRepository
import com.example.gymapplktrack.domain.model.ActivitySummary
import com.example.gymapplktrack.domain.model.DailyLog
import com.example.gymapplktrack.domain.model.StreakMode
import com.example.gymapplktrack.domain.model.StreakInfo
import com.example.gymapplktrack.domain.model.ThemePreference
import com.example.gymapplktrack.domain.model.UserPreferences
import com.example.gymapplktrack.domain.model.WeightUnit
import com.example.gymapplktrack.domain.util.CalendarUtils
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: GymRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val selectedMonth = MutableStateFlow(YearMonth.now())

    private val streakFlow = preferencesRepository.preferences.flatMapLatest { prefs ->
        repository.observeStreak(prefs.streakMode)
    }

    private val summaryFlow = selectedMonth.flatMapLatest { month ->
        repository.observeProfileSummary(month)
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        repository.observeDailyLogs(),
        selectedMonth,
        preferencesRepository.preferences,
        streakFlow,
        summaryFlow
    ) { logs, month, preferences, streak, summary ->
        val workoutDays = logs.filter { it.didWorkout }
            .map { it.date }
        val creatineDays = logs.filter { it.tookCreatine }.map { it.date }
        ProfileUiState(
            month = month,
            logs = logs,
            workoutMarkedDays = CalendarUtils.markDays(workoutDays, month),
            creatineMarkedDays = CalendarUtils.markDays(creatineDays, month),
            streakInfo = streak,
            preferences = preferences,
            summary = summary
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun previousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }

    fun toggleCreatine(date: LocalDate) {
        viewModelScope.launch {
            val logs = repository.observeDailyLogs().first()
            val current = logs.find { it.date == date }?.tookCreatine ?: false
            repository.toggleCreatine(date, !current)
        }
    }

    fun ensureWorkoutLog(date: LocalDate) {
        viewModelScope.launch { repository.ensureWorkoutLog(date) }
    }

    fun updateTheme(theme: ThemePreference) {
        viewModelScope.launch { preferencesRepository.updateTheme(theme) }
    }

    fun updateUnit(unit: WeightUnit) {
        viewModelScope.launch { preferencesRepository.updateUnit(unit) }
    }

    fun updateStreakMode(mode: StreakMode) {
        viewModelScope.launch { preferencesRepository.updateStreakMode(mode) }
    }

    fun updateCreatineReminder(enabled: Boolean, time: LocalTime?) {
        viewModelScope.launch { preferencesRepository.updateCreatineReminder(enabled, time) }
    }

    companion object {
        fun provideFactory(container: AppContainer) = viewModelFactory {
            initializer { ProfileViewModel(container.repository, container.preferencesRepository) }
        }
    }
}

data class ProfileUiState(
    val month: YearMonth = YearMonth.now(),
    val logs: List<DailyLog> = emptyList(),
    val workoutMarkedDays: Set<Int> = emptySet(),
    val creatineMarkedDays: Set<Int> = emptySet(),
    val streakInfo: StreakInfo? = null,
    val preferences: UserPreferences? = null,
    val summary: ActivitySummary? = null
)
