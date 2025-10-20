package com.example.gymapplktrack.ui.features.workout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gymapplktrack.domain.model.ExerciseOverview
import com.example.gymapplktrack.domain.model.WorkoutExercise
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WorkoutScreen(
    state: WorkoutUiState,
    events: Flow<WorkoutEvent>,
    onBack: () -> Unit,
    onAddExercise: (Long) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long, Float, Int) -> Unit,
    onRemoveSet: (Long, Int) -> Unit,
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
                is WorkoutEvent.RoutineSaved -> snackbarHostState.showSnackbar("Rutina \"${event.name}\" guardada")
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Entreno activo", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (state.workout == null) {
            EmptyWorkoutState(modifier = Modifier
                .fillMaxSize()
                .padding(padding),
                onDiscard = onDiscard
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActiveWorkoutHeader(state)
                AddExerciseSection(
                    available = state.availableExercises,
                    workout = state.workout,
                    onAddExercise = onAddExercise
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
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
                    textStyle = MaterialTheme.typography.bodyLarge,
                    label = { Text("Notas del entreno") }
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDiscard,
                        modifier = Modifier.weight(1f)
                    ) { Text("Descartar") }
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.weight(1f)
                    ) { Text("Terminar") }
                }
            }
        }
    }
}

@Composable
private fun EmptyWorkoutState(modifier: Modifier = Modifier, onDiscard: () -> Unit) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sin entreno activo",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Vuelve a la pantalla de rutinas para iniciar un nuevo entreno.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = onDiscard) {
            Text("Volver")
        }
    }
}

@Composable
private fun ActiveWorkoutHeader(state: WorkoutUiState) {
    Surface(
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("INTENSIDAD", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Text(
                text = if (state.workout?.routineId == null) "Entreno libre" else "${state.workout.exercises.size} ejercicios preparados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Series registradas: ${state.workout?.exercises?.sumOf { it.sets.size } ?: 0}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AddExerciseSection(
    available: List<ExerciseOverview>,
    workout: com.example.gymapplktrack.domain.model.WorkoutInProgress,
    onAddExercise: (Long) -> Unit
) {
    val availableToAdd = available.filterNot { exercise -> workout.exercises.any { it.exerciseId == exercise.id } }
    if (availableToAdd.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Agregar potencia",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableToAdd.forEach { exercise ->
                AssistChip(
                    onClick = { onAddExercise(exercise.id) },
                    label = { Text(exercise.name.uppercase()) },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        leadingIconContentColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }
    }
}

@Composable
private fun WorkoutExerciseCard(
    exercise: WorkoutExercise,
    onAddSet: (Long, Float, Int) -> Unit,
    onRemoveSet: (Long, Int) -> Unit,
    onRemoveExercise: (Long) -> Unit
) {
    var weightInput by rememberSaveable(exercise.exerciseId, "weight") { mutableStateOf("") }
    var repsInput by rememberSaveable(exercise.exerciseId, "reps") { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(exercise.name.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${exercise.sets.size} series acumuladas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { onRemoveExercise(exercise.exerciseId) }) {
                    Text("Quitar")
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (exercise.sets.isEmpty()) {
                    Text("Aún sin series. Empieza a cargar peso.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    exercise.sets.forEachIndexed { index, set ->
                        Surface(
                            tonalElevation = 2.dp,
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItemPlacement()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Serie ${index + 1}", style = MaterialTheme.typography.labelLarge)
                                    Text("${set.weightKg} kg  ×  ${set.reps} reps", style = MaterialTheme.typography.bodyMedium)
                                }
                                IconButton(onClick = { onRemoveSet(exercise.exerciseId, index) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Eliminar serie")
                                }
                            }
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    val weight = weightInput.toFloatOrNull()
                    val reps = repsInput.toIntOrNull()
                    if (weight != null && reps != null) {
                        onAddSet(exercise.exerciseId, weight, reps)
                        weightInput = ""
                        repsInput = ""
                    }
                }) {
                    Text("Agregar serie")
                }
                OutlinedButton(onClick = { onRemoveExercise(exercise.exerciseId) }) {
                    Text("Eliminar ejercicio")
                }
            }
        }
    }
}
