package com.example.gymapplktrack.ui.features.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gymapplktrack.AppContainer
import com.example.gymapplktrack.data.repository.GymRepository
import com.example.gymapplktrack.domain.model.ExerciseOverview
import com.example.gymapplktrack.domain.model.WorkoutExercise
import com.example.gymapplktrack.domain.model.WorkoutInProgress
import com.example.gymapplktrack.domain.model.WorkoutSet
import com.example.gymapplktrack.domain.model.WorkoutSummary
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class WorkoutViewModel(private val repository: GymRepository) : ViewModel() {
    private val draft = MutableStateFlow<WorkoutInProgress?>(null)
    private val notes = MutableStateFlow("")
    private val lastSummary = MutableStateFlow<WorkoutSummary?>(null)
    private val lastCompletedWorkout = MutableStateFlow<WorkoutInProgress?>(null)
    private val _events = MutableSharedFlow<WorkoutEvent>()
    val events = _events

    private val availableExercisesFlow = repository.observeExercises()
    private val persistedWorkoutFlow = repository.observeActiveWorkout()

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<WorkoutUiState> = combine(
        availableExercisesFlow,
        persistedWorkoutFlow,
        draft,
        notes,
        lastSummary,
        lastCompletedWorkout
    ) { values ->
        val available = values[0] as List<ExerciseOverview>
        val persisted = values[1] as WorkoutInProgress?
        val currentDraft = values[2] as WorkoutInProgress?
        val note = values[3] as String
        val summary = values[4] as WorkoutSummary?
        val completed = values[5] as WorkoutInProgress?

        val active = currentDraft ?: persisted
        val canSaveRoutine = completed?.routineId == null &&
            (completed?.exercises?.any { it.sets.isNotEmpty() } == true)
        WorkoutUiState(
            availableExercises = available,
            workout = active,
            notes = note,
            hasSavedWorkout = persisted != null && currentDraft == null,
            lastSummary = summary,
            completedWorkout = completed,
            canSaveCompletedWorkout = canSaveRoutine
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutUiState()
    )

    init {
        viewModelScope.launch {
            persistedWorkoutFlow.collect { active ->
                if (active != null && draft.value == null) {
                    draft.value = active
                }
            }
        }
    }

    fun startRoutine(routineId: Long?) {
        viewModelScope.launch {
            val exercises = availableExercisesFlow.first()
            val selected = if (routineId != null) {
                val routine = repository.observeRoutine(routineId).first()
                routine?.exercises?.sortedBy { it.order }?.mapNotNull { item ->
                    exercises.find { it.id == item.exerciseId }?.let { overview ->
                        WorkoutExercise(overview.id, overview.name, emptyList())
                    }
                } ?: emptyList()
            } else {
                emptyList()
            }
            val workout = WorkoutInProgress(
                routineId = routineId,
                startDate = LocalDate.now(),
                startTime = LocalTime.now(),
                exercises = selected
            )
            draft.value = workout
            repository.saveActiveWorkout(workout)
        }
    }

    fun setNotes(value: String) {
        notes.value = value
    }

    fun addExerciseToWorkout(exerciseId: Long) {
        viewModelScope.launch {
            val current = draft.value ?: return@launch
            val available = availableExercisesFlow.first()
            val exercise = available.find { it.id == exerciseId } ?: return@launch
            if (current.exercises.any { it.exerciseId == exerciseId }) {
                _events.emit(WorkoutEvent.Error("El ejercicio ya está en la rutina"))
                return@launch
            }
            val updated = current.copy(exercises = current.exercises + WorkoutExercise(exerciseId, exercise.name, emptyList()))
            draft.value = updated
            repository.saveActiveWorkout(updated)
        }
    }

    fun removeExercise(exerciseId: Long) {
        viewModelScope.launch {
            val current = draft.value ?: return@launch
            val updated = current.copy(exercises = current.exercises.filterNot { it.exerciseId == exerciseId })
            draft.value = updated
            repository.saveActiveWorkout(updated)
        }
    }

    fun addSet(exerciseId: Long, weight: Float, reps: Int) {
        if (weight <= 0 || reps <= 0) {
            viewModelScope.launch { _events.emit(WorkoutEvent.Error("Peso y repeticiones deben ser mayores a 0")) }
            return
        }
        viewModelScope.launch {
            val current = draft.value ?: return@launch
            val updated = current.copy(
                exercises = current.exercises.map { exercise ->
                    if (exercise.exerciseId == exerciseId) {
                        exercise.copy(sets = exercise.sets + WorkoutSet(weight, reps))
                    } else {
                        exercise
                    }
                }
            )
            draft.value = updated
            repository.saveActiveWorkout(updated)
        }
    }

    fun removeSet(exerciseId: Long, index: Int) {
        viewModelScope.launch {
            val current = draft.value ?: return@launch
            val updated = current.copy(
                exercises = current.exercises.map { exercise ->
                    if (exercise.exerciseId == exerciseId && index in exercise.sets.indices) {
                        val mutableSets = exercise.sets.toMutableList()
                        mutableSets.removeAt(index)
                        exercise.copy(sets = mutableSets)
                    } else {
                        exercise
                    }
                }
            )
            draft.value = updated
            repository.saveActiveWorkout(updated)
        }
    }

    fun finishWorkout() {
        viewModelScope.launch {
            val current = draft.value ?: run {
                _events.emit(WorkoutEvent.Error("No hay un entreno activo"))
                return@launch
            }
            val summary = repository.completeWorkout(
                state = current,
                endTime = LocalTime.now(),
                notes = notes.value.takeIf { it.isNotBlank() }
            )
            draft.value = null
            notes.value = ""
            lastSummary.value = summary
            lastCompletedWorkout.value = current
            _events.emit(WorkoutEvent.Completed(summary))
        }
    }

    fun discardWorkout() {
        viewModelScope.launch {
            draft.value = null
            notes.value = ""
            repository.clearActiveWorkout()
        }
    }

    fun clearSummary() {
        lastSummary.value = null
        lastCompletedWorkout.value = null
    }

    fun saveCompletedWorkoutAsRoutine(name: String) {
        viewModelScope.launch {
            val completed = lastCompletedWorkout.value ?: run {
                _events.emit(WorkoutEvent.Error("No hay un entreno listo para guardar"))
                return@launch
            }
            val routineName = name.trim()
            if (routineName.isBlank()) {
                _events.emit(WorkoutEvent.Error("Necesitas un nombre para la rutina"))
                return@launch
            }
            val exerciseOrder = completed.exercises
                .filter { it.sets.isNotEmpty() }
                .map { it.exerciseId }
                .distinct()
            if (exerciseOrder.isEmpty()) {
                _events.emit(WorkoutEvent.Error("No hay series registradas para guardar"))
                return@launch
            }
            val routineId = repository.upsertRoutine(null, routineName, exerciseOrder)
            _events.emit(WorkoutEvent.RoutineSaved(routineName, routineId))
            lastCompletedWorkout.value = completed.copy(routineId = routineId)
        }
    }

    companion object {
        fun provideFactory(container: AppContainer) = viewModelFactory {
            initializer { WorkoutViewModel(container.repository) }
        }
    }
}

data class WorkoutUiState(
    val availableExercises: List<ExerciseOverview> = emptyList(),
    val workout: WorkoutInProgress? = null,
    val notes: String = "",
    val hasSavedWorkout: Boolean = false,
    val lastSummary: WorkoutSummary? = null,
    val completedWorkout: WorkoutInProgress? = null,
    val canSaveCompletedWorkout: Boolean = false
)

sealed class WorkoutEvent {
    data class Completed(val summary: WorkoutSummary) : WorkoutEvent()
    data class RoutineSaved(val name: String, val routineId: Long) : WorkoutEvent()
    data class Error(val message: String) : WorkoutEvent()
}
