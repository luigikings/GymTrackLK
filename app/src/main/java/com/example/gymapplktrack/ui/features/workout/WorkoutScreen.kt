package com.example.gymapplktrack.ui.features.workout

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gymapplktrack.domain.model.ExerciseOverview
import com.example.gymapplktrack.domain.model.WorkoutExercise
import com.example.gymapplktrack.domain.model.WorkoutSummary
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    state: WorkoutUiState,
    events: Flow<WorkoutEvent>,
    onBack: () -> Unit,
    onAddExercise: (Long) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long, Float, Int) -> Unit,
    onRemoveSet: (Long) -> Unit,
    onFinish: () -> Unit,
    onDiscard: () -> Unit,
    onNotesChange: (String) -> Unit,
    onShowSummary: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is WorkoutEvent.Error -> snackbarHostState.showSnackbar(event.message)
                is WorkoutEvent.Completed -> onShowSummary()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entreno activo") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.workout == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Selecciona una rutina para comenzar")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onDiscard) { Text("Volver") }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AddExerciseSection(available = state.availableExercises, workout = state.workout, onAddExercise = onAddExercise)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    items(state.workout.exercises, key = { it.exerciseId }) { exercise ->
                        WorkoutExerciseCard(
                            exercise = exercise,
                            onAddSet = onAddSet,
                            onRemoveSet = onRemoveSet,
                            onRemoveExercise = onRemoveExercise
                        )
                    }
                }
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notas del entreno") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDiscard, modifier = Modifier.weight(1f)) { Text("Descartar") }
                    Button(onClick = onFinish, modifier = Modifier.weight(1f)) { Text("Terminar") }
                }
            }
        }
    }
}

@Composable
private fun AddExerciseSection(
    available: List<ExerciseOverview>,
    workout: com.example.gymapplktrack.domain.model.WorkoutInProgress,
    onAddExercise: (Long) -> Unit
) {
    val availableToAdd = available.filterNot { exercise -> workout.exercises.any { it.exerciseId == exercise.id } }
    if (availableToAdd.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Agregar ejercicio", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.height(120.dp)) {
            items(availableToAdd, key = { it.id }) { exercise ->
                OutlinedButton(onClick = { onAddExercise(exercise.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text(exercise.name)
                }
            }
        }
    }
}

@Composable
private fun WorkoutExerciseCard(
    exercise: WorkoutExercise,
    onAddSet: (Long, Float, Int) -> Unit,
    onRemoveSet: (Long) -> Unit,
    onRemoveExercise: (Long) -> Unit
) {
    var weightInput by rememberSaveable(exercise.exerciseId, "weight") { mutableStateOf("") }
    var repsInput by rememberSaveable(exercise.exerciseId, "reps") { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = { onRemoveExercise(exercise.exerciseId) }) { Text("Quitar") }
        }
        if (exercise.sets.isEmpty()) {
            Text("Sin series aún", style = MaterialTheme.typography.bodySmall)
        } else {
            exercise.sets.forEachIndexed { index, set ->
                Text("Serie ${index + 1}: ${set.weightKg} kg × ${set.reps}")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                modifier = Modifier.weight(1f),
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = repsInput,
                onValueChange = { repsInput = it },
                modifier = Modifier.weight(1f),
                label = { Text("Reps") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val weight = weightInput.toFloatOrNull()
                val reps = repsInput.toIntOrNull()
                if (weight != null && reps != null) {
                    onAddSet(exercise.exerciseId, weight, reps)
                    weightInput = ""
                    repsInput = ""
                }
            }) { Text("Agregar serie") }
            OutlinedButton(onClick = { onRemoveSet(exercise.exerciseId) }) { Text("Eliminar última") }
        }
    }
}
