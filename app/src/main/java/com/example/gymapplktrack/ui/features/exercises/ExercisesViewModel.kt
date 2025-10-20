package com.example.gymapplktrack.ui.features.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gymapplktrack.AppContainer
import com.example.gymapplktrack.data.repository.GymRepository
import com.example.gymapplktrack.domain.model.ExerciseOverview
import com.example.gymapplktrack.domain.model.ExerciseSort
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExercisesViewModel(private val repository: GymRepository) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val categoryFilter = MutableStateFlow<String?>(null)
    private val sortOption = MutableStateFlow(ExerciseSort.ALPHABETICAL)

    private val _events = MutableSharedFlow<ExercisesEvent>()
    val events = _events

    val uiState: StateFlow<ExerciseListUiState> = combine(
        repository.observeExercises(),
        repository.observeCategories(),
        searchQuery,
        categoryFilter,
        sortOption
    ) { exercises, categories, query, category, sort ->
        val filtered = exercises.filter { exercise ->
            exercise.name.contains(query, ignoreCase = true) &&
                (category == null || exercise.category == category)
        }
        val sorted = when (sort) {
            ExerciseSort.ALPHABETICAL -> filtered.sortedBy { it.name.lowercase() }
            ExerciseSort.MOST_USED -> filtered.sortedWith(compareByDescending<ExerciseOverview> { it.usageCount }.thenBy { it.name })
            ExerciseSort.RECORD_FIRST -> filtered.sortedWith(
                compareByDescending<ExerciseOverview> { it.personalRecord != null }
                    .thenByDescending { it.personalRecord?.bestWeightKg ?: 0f }
                    .thenByDescending { it.personalRecord?.bestReps ?: 0 }
                    .thenBy { it.name }
            )
        }
        ExerciseListUiState(
            isLoading = false,
            exercises = sorted,
            searchQuery = query,
            selectedCategory = category,
            sort = sort,
            categories = categories
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExerciseListUiState(isLoading = true)
    )

    fun onSearchChange(query: String) {
        searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        categoryFilter.value = category
    }

    fun onSortSelected(sort: ExerciseSort) {
        sortOption.value = sort
    }

    fun createExercise(name: String, category: String?, imageUri: String?, notes: String?) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _events.emit(ExercisesEvent.Error("El nombre es obligatorio"))
                return@launch
            }
            val id = repository.insertExercise(name.trim(), category?.takeIf { it.isNotBlank() }, imageUri, notes)
            _events.emit(ExercisesEvent.ExerciseSaved(id))
        }
    }

    fun deleteExercise(id: Long) {
        viewModelScope.launch {
            repository.deleteExercise(id)
            _events.emit(ExercisesEvent.Message("Ejercicio eliminado"))
        }
    }

    fun updateNotes(exerciseId: Long, notes: String?) {
        viewModelScope.launch {
            repository.updateExerciseNotes(exerciseId, notes)
            _events.emit(ExercisesEvent.Message("Notas actualizadas"))
        }
    }

    fun updateImage(exerciseId: Long, imageUri: String?) {
        viewModelScope.launch {
            repository.updateExerciseImage(exerciseId, imageUri)
            _events.emit(ExercisesEvent.Message("Imagen actualizada"))
        }
    }

    fun updateExerciseDetails(exercise: ExerciseOverview, name: String, category: String?, notes: String?) {
        viewModelScope.launch {
            val updated = exercise.copy(
                name = name,
                category = category,
                notes = notes
            )
            repository.updateExercise(updated)
            _events.emit(ExercisesEvent.Message("Ejercicio actualizado"))
        }
    }

    companion object {
        fun provideFactory(container: AppContainer) = viewModelFactory {
            initializer { ExercisesViewModel(container.repository) }
        }
    }
}

data class ExerciseListUiState(
    val isLoading: Boolean = false,
    val exercises: List<ExerciseOverview> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val sort: ExerciseSort = ExerciseSort.ALPHABETICAL,
    val categories: List<String> = emptyList()
)

sealed class ExercisesEvent {
    data class ExerciseSaved(val id: Long) : ExercisesEvent()
    data class Message(val text: String) : ExercisesEvent()
    data class Error(val message: String) : ExercisesEvent()
}
