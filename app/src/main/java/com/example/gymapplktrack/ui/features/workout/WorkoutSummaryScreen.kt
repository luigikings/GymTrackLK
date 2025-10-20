package com.example.gymapplktrack.ui.features.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymapplktrack.domain.model.WorkoutSummary
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummaryScreen(
    state: WorkoutUiState,
    events: Flow<WorkoutEvent>,
    onBack: () -> Unit,
    onShare: (WorkoutSummary) -> Unit,
    onSaveRoutine: (String) -> Unit
) {
    val summary = state.lastSummary
    val snackbarHostState = remember { SnackbarHostState() }
    var showSaveDialog by remember { mutableStateOf(false) }
    var routineName by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is WorkoutEvent.RoutineSaved -> {
                    snackbarHostState.showSnackbar("Rutina \"${event.name}\" guardada")
                    showSaveDialog = false
                    routineName = ""
                }
                is WorkoutEvent.Error -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = {
                showSaveDialog = false
            },
            confirmButton = {
                Button(onClick = { onSaveRoutine(routineName) }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Nombrar rutina") },
            text = {
                OutlinedTextField(
                    value = routineName,
                    onValueChange = { routineName = it },
                    label = { Text("Nombre del entreno") }
                )
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Entreno completado", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (summary == null) {
                Text("No hay resumen disponible", style = MaterialTheme.typography.titleMedium)
            } else {
                Surface(
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("¡Victoria lograda!", style = MaterialTheme.typography.displaySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            SummaryStat(label = "Duración", value = "${summary.duration.toMinutes()} min")
                            SummaryStat(label = "Ejercicios", value = summary.totalExercises.toString())
                            SummaryStat(label = "Series", value = summary.totalSets.toString())
                        }
                        if (summary.brokenRecords.isNotEmpty()) {
                            Text("Récords batidos", style = MaterialTheme.typography.titleMedium)
                            summary.brokenRecords.forEach { record ->
                                Text(
                                    text = "${record.exerciseName}: ${record.weightKg} kg × ${record.reps}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            Text(
                                text = "Sin nuevos récords esta vez, pero el esfuerzo cuenta.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                if (state.canSaveCompletedWorkout) {
                    Surface(
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "¿Deseas guardar este entreno?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Se convertirá en una nueva rutina con el orden de ejercicios que acabas de completar.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = { showSaveDialog = true }) {
                                    Text("Guardar como rutina")
                                }
                                TextButton(onClick = { showSaveDialog = false }) {
                                    Text("No guardar")
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { onShare(summary) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Compartir resumen")
                }
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver")
                }
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
