package com.example.gymapplktrack.ui.features.exercises

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gymapplktrack.R
import com.example.gymapplktrack.domain.model.ExerciseOverview
import com.example.gymapplktrack.domain.model.ExerciseSort
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    state: ExerciseListUiState,
    events: Flow<ExercisesEvent>,
    onSearchChange: (String) -> Unit,
    onSortChange: (ExerciseSort) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onAddExercise: () -> Unit,
    onExerciseClick: (Long) -> Unit,
    onDeleteExercise: (Long) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is ExercisesEvent.Error -> snackbarHostState.showSnackbar(event.message)
                is ExercisesEvent.Message -> snackbarHostState.showSnackbar(event.text)
                is ExercisesEvent.ExerciseSaved -> snackbarHostState.showSnackbar("Ejercicio guardado")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExercise, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Agregar ejercicio")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Ejercicios",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            SortSelector(selected = state.sort, onSelect = onSortChange)
            Spacer(modifier = Modifier.height(12.dp))
            CategoryFilter(categories = state.categories, selected = state.selectedCategory, onChange = onCategoryChange)
            Spacer(modifier = Modifier.height(16.dp))
            if (state.exercises.isEmpty()) {
                EmptyExerciseState()
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
                    items(state.exercises, key = { it.id }) { exercise ->
                        ExerciseListItem(
                            exercise = exercise,
                            onClick = { onExerciseClick(exercise.id) },
                            onDelete = { pendingDelete = exercise.id }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    pendingDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteExercise(id)
                    pendingDelete = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") }
            },
            title = { Text("¿Eliminar ejercicio?") },
            text = { Text("Esta acción no se puede deshacer.") }
        )
    }
}

@Composable
private fun SortSelector(selected: ExerciseSort, onSelect: (ExerciseSort) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ExerciseSort.values().forEach { sort ->
            AssistChip(
                onClick = { onSelect(sort) },
                label = { Text(sortLabel(sort)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected == sort) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                )
            )
        }
    }
}

private fun sortLabel(sort: ExerciseSort): String = when (sort) {
    ExerciseSort.ALPHABETICAL -> "A-Z"
    ExerciseSort.MOST_USED -> "Más usados"
    ExerciseSort.RECORD_FIRST -> "Con récord"
}

@Composable
private fun CategoryFilter(categories: List<String>, selected: String?, onChange: (String?) -> Unit) {
    if (categories.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = { onChange(null) },
            label = { Text("Todas") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (selected == null) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
            )
        )
        categories.forEach { category ->
            AssistChip(
                onClick = { onChange(category) },
                label = { Text(category) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected == category) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun ExerciseListItem(exercise: ExerciseOverview, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ExerciseImage(imageUri = exercise.imageUri)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                exercise.personalRecord?.let { record ->
                    Text(
                        text = "Record: ${record.bestWeightKg} kg × ${record.bestReps}",
                        style = MaterialTheme.typography.bodySmall
                    )
                } ?: Text("Sin record", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Usos: ${exercise.usageCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            TextButton(onClick = onDelete) { Text("Eliminar") }
        }
    }
}

@Composable
private fun ExerciseImage(imageUri: String?, size: Int = 72) {
    val modifier = Modifier
        .size(size.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant)
    if (imageUri != null) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Imagen del ejercicio",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Sin imagen",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun EmptyExerciseState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Aún no tienes ejercicios", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Pulsa el botón + para agregar tu primer ejercicio.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
