package com.example.gymapplktrack.ui.features.routines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.createSavedStateHandle
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gymapplktrack.AppContainer
import com.example.gymapplktrack.data.repository.GymRepository
import com.example.gymapplktrack.domain.model.ExerciseOverview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class RoutineEditorViewModel(
    private val repository: GymRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val routineId: Long? = savedStateHandle.get<Long?>("routineId")
    private val name = MutableStateFlow("")
    private val selectedExerciseIds = MutableStateFlow<List<Long>>(emptyList())
    private val _events = MutableSharedFlow<RoutineEditorEvent>()
    val events = _events

    private val availableExercisesFlow = repository.observeExercises()

    val uiState: StateFlow<RoutineEditorUiState> = combine(
        availableExercisesFlow,
        selectedExerciseIds,
        name
    ) { available, selected, routineName ->
        val selectedItems = selected.mapIndexed { index, id ->
            val exercise = available.find { it.id == id }
            RoutineExerciseDraft(id = id, name = exercise?.name ?: "Ejercicio", order = index)
        }
        RoutineEditorUiState(
            name = routineName,
            available = available,
            selected = selectedItems
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RoutineEditorUiState()
    )

    init {
        if (routineId != null) {
            viewModelScope.launch {
                val routine = repository.observeRoutine(routineId).first()
                routine?.let {
                    name.value = it.name
                    selectedExerciseIds.value = it.exercises.sortedBy { item -> item.order }.map { item -> item.exerciseId }
                }
            }
        }
    }

    fun updateName(value: String) {
        name.value = value
    }

    fun addExercise(exerciseId: Long) {
        selectedExerciseIds.value = selectedExerciseIds.value + exerciseId
    }

    fun removeExercise(exerciseId: Long) {
        selectedExerciseIds.value = selectedExerciseIds.value.filterNot { it == exerciseId }
    }

    fun reorderExercise(fromIndex: Int, toIndex: Int) {
        val list = selectedExerciseIds.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            selectedExerciseIds.value = list
        }
    }

    fun saveRoutine() {
        viewModelScope.launch {
            val currentName = name.value.trim()
            if (currentName.isBlank()) {
                _events.emit(RoutineEditorEvent.Error("El nombre es obligatorio"))
                return@launch
            }
            val exercises = selectedExerciseIds.value
            if (exercises.isEmpty()) {
                _events.emit(RoutineEditorEvent.Error("Selecciona al menos un ejercicio"))
                return@launch
            }
            val id = repository.upsertRoutine(routineId, currentName, exercises)
            _events.emit(RoutineEditorEvent.Saved(id))
        }
    }

    companion object {
        fun provideFactory(container: AppContainer) = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                RoutineEditorViewModel(container.repository, savedStateHandle)
            }
        }
    }
}

data class RoutineEditorUiState(
    val name: String = "",
    val available: List<ExerciseOverview> = emptyList(),
    val selected: List<RoutineExerciseDraft> = emptyList()
)

data class RoutineExerciseDraft(
    val id: Long,
    val name: String,
    val order: Int
)

sealed class RoutineEditorEvent {
    data class Saved(val routineId: Long) : RoutineEditorEvent()
    data class Error(val message: String) : RoutineEditorEvent()
}
