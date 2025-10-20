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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Rutinas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (workoutState.workout != null) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Entreno en progreso", style = MaterialTheme.typography.titleMedium)
                    Text("Ejercicios activos: ${workoutState.workout.exercises.size}")
                    Button(onClick = onResumeWorkout) { Text("Continuar") }
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
                    Text("Crear rutina")
                }
                OutlinedButton(onClick = onStartFree) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Entreno libre")
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(routine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${routine.exercises.size} ejercicios")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onStart) { Text("Iniciar") }
                OutlinedButton(onClick = onEdit) { Text("Editar") }
                OutlinedButton(onClick = onDelete) { Text("Eliminar") }
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
        Text("No tienes rutinas aún", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCreateRoutine) { Text("Crear rutina") }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onStartFree) { Text("Entreno libre") }
    }
}
