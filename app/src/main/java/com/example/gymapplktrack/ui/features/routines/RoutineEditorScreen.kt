package com.example.gymapplktrack.ui.features.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymapplktrack.domain.model.ExerciseOverview
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorScreen(
    state: RoutineEditorUiState,
    events: Flow<RoutineEditorEvent>,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onAddExercise: (Long) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onMoveExercise: (Int, Int) -> Unit,
    onSave: () -> Unit,
    onSaved: (Long) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is RoutineEditorEvent.Error -> snackbarHostState.showSnackbar(event.message)
                is RoutineEditorEvent.Saved -> onSaved(event.routineId)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Diseñar rutina", style = MaterialTheme.typography.titleLarge) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Nombre del plan", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Hazlo intimidante") }
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Arsenal disponible", style = MaterialTheme.typography.titleMedium)
                    ExerciseSelector(
                        available = state.available,
                        selectedIds = state.selected.map { it.id }.toSet(),
                        onToggle = { exerciseId, isSelected ->
                            if (isSelected) onAddExercise(exerciseId) else onRemoveExercise(exerciseId)
                        }
                    )
                }
            }
            if (state.selected.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Orden de ataque", style = MaterialTheme.typography.titleMedium)
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(state.selected, key = { it.id }) { item ->
                                val canMoveUp = item.order > 0
                                val canMoveDown = item.order < state.selected.lastIndex
                                RoutineOrderRow(
                                    item = item,
                                    canMoveUp = canMoveUp,
                                    canMoveDown = canMoveDown,
                                    onMoveUp = { onMoveExercise(item.order, (item.order - 1).coerceAtLeast(0)) },
                                    onMoveDown = { onMoveExercise(item.order, (item.order + 1).coerceAtMost(state.selected.lastIndex)) },
                                    onRemove = { onRemoveExercise(item.id) }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar rutina")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseSelector(
    available: List<ExerciseOverview>,
    selectedIds: Set<Long>,
    onToggle: (Long, Boolean) -> Unit
) {
    if (available.isEmpty()) {
        Text("Agrega ejercicios desde la pestaña principal", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        available.forEach { exercise ->
            val selected = selectedIds.contains(exercise.id)
            AssistChip(
                onClick = { onToggle(exercise.id, !selected) },
                label = { Text(exercise.name.uppercase()) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun RoutineOrderRow(
    item: RoutineExerciseDraft,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${item.order + 1}. ${item.name}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir")
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar")
            }
            TextButton(onClick = onRemove) { Text("Quitar") }
        }
    }
}
