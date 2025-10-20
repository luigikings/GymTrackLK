package com.example.gymapplktrack.ui.features.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gymapplktrack.AppContainer
import com.example.gymapplktrack.data.repository.GymRepository
import com.example.gymapplktrack.domain.model.RoutineOverview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoutinesViewModel(private val repository: GymRepository) : ViewModel() {
    private val _events = MutableSharedFlow<RoutineEvent>()
    val events = _events

    val uiState: StateFlow<RoutineListUiState> = repository.observeRoutines()
        .map { routines ->
            RoutineListUiState(
                isLoading = false,
                routines = routines
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RoutineListUiState()
        )

    fun deleteRoutine(id: Long) {
        viewModelScope.launch {
            repository.deleteRoutine(id)
            _events.emit(RoutineEvent.Message("Rutina eliminada"))
        }
    }

    companion object {
        fun provideFactory(container: AppContainer) = viewModelFactory {
            initializer { RoutinesViewModel(container.repository) }
        }
    }
}

data class RoutineListUiState(
    val isLoading: Boolean = true,
    val routines: List<RoutineOverview> = emptyList()
)

sealed class RoutineEvent {
    data class Message(val message: String) : RoutineEvent()
}
