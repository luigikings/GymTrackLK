package com.example.gymapplktrack.ui.features.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gymapplktrack.AppContainer
import com.example.gymapplktrack.data.repository.GymRepository
import com.example.gymapplktrack.domain.model.ExerciseDetail
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ExerciseDetailViewModel(
    repository: GymRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val exerciseId: Long = savedStateHandle.get<Long>("exerciseId") ?: 0

    val uiState: StateFlow<ExerciseDetailUiState> = repository.observeExerciseDetail(exerciseId)
        .filterNotNull()
        .map { detail -> ExerciseDetailUiState(detail = detail) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExerciseDetailUiState()
        )

    companion object {
        fun provideFactory(container: AppContainer) = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                ExerciseDetailViewModel(container.repository, savedStateHandle)
            }
        }
    }
}

data class ExerciseDetailUiState(
    val detail: ExerciseDetail? = null
)
