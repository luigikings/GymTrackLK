package com.example.gymapplktrack.ui.features.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymapplktrack.domain.model.RoutineOverview
import com.example.gymapplktrack.ui.features.workout.WorkoutUiState

@Composable
fun RoutinesScreen(
    state: RoutineListUiState,
    workoutState: WorkoutUiState,
    onCreateRoutine: () -> Unit,
    onStartRoutine: (Long) -> Unit,
    onStartFree: () -> Unit,
    onEditRoutine: (Long) -> Unit,
    onDeleteRoutine: (Long) -> Unit,
    onResumeWorkout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Tus rituales", style = MaterialTheme.typography.displaySmall)
        if (workoutState.workout != null) {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Entreno en progreso", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Ejercicios activos: ${workoutState.workout.exercises.size}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onResumeWorkout) { Text("Retomar intensidad") }
                }
            }
        }
        if (state.routines.isEmpty()) {
            EmptyRoutineState(onCreateRoutine = onCreateRoutine, onStartFree = onStartFree)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCreateRoutine) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nueva rutina")
                }
                OutlinedButton(onClick = onStartFree) {
                    Icon(Icons.Default.FlashOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Entreno libre")
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(state.routines, key = { it.id }) { routine ->
                    RoutineCard(
                        routine = routine,
                        onStart = { onStartRoutine(routine.id) },
                        onEdit = { onEditRoutine(routine.id) },
                        onDelete = { onDeleteRoutine(routine.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineCard(routine: RoutineOverview, onStart: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(routine.name.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "${routine.exercises.size} ejercicios asignados",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onStart) { Text("Ejecutar") }
                OutlinedButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onDelete) { Text("Borrar") }
            }
        }
    }
}

@Composable
private fun EmptyRoutineState(onCreateRoutine: () -> Unit, onStartFree: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sin rituales todavía", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCreateRoutine) { Text("Crear tu primera rutina") }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onStartFree) { Text("Entreno libre") }
    }
}
