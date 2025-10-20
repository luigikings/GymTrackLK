package com.example.gymapplktrack.ui.features.routines

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymapplktrack.domain.model.ExerciseOverview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorScreen(
    state: RoutineEditorUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onAddExercise: (Long) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onMoveExercise: (Int, Int) -> Unit,
    onSave: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar rutina") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre de la rutina") }
            )
            Text("Selecciona ejercicios", style = MaterialTheme.typography.titleMedium)
            ExerciseSelector(
                available = state.available,
                selectedIds = state.selected.map { it.id }.toSet(),
                onToggle = { exerciseId, selected ->
                    if (selected) onAddExercise(exerciseId) else onRemoveExercise(exerciseId)
                }
            )
            if (state.selected.isNotEmpty()) {
                Text("Orden actual", fontWeight = FontWeight.SemiBold)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.selected, key = { it.id }) { item ->
                        Text("${item.order + 1}. ${item.name}")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Guardar rutina") }
        }
    }
}

@Composable
private fun ExerciseSelector(
    available: List<ExerciseOverview>,
    selectedIds: Set<Long>,
    onToggle: (Long, Boolean) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(available, key = { it.id }) { exercise ->
            val checked = selectedIds.contains(exercise.id)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checked, onCheckedChange = { onToggle(exercise.id, it) })
                Text(exercise.name)
            }
            Divider()
        }
    }
}
