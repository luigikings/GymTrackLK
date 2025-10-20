package com.example.gymapplktrack.ui.features.exercises

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gymapplktrack.domain.model.ExerciseDetail
import com.example.gymapplktrack.domain.model.ExerciseSetHistory
import com.example.gymapplktrack.ui.components.MonthCalendar
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    state: ExerciseDetailUiState,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onChangeImage: (Long, String?) -> Unit,
    onUpdateNotes: (Long, String?) -> Unit,
    onViewHistory: (Long) -> Unit,
    onUpdateDetails: (Long, String, String?, String?) -> Unit
) {
    val detail = state.detail ?: return
    val snackbarHostState = remember { SnackbarHostState() }
    var notes by rememberSaveable { mutableStateOf(detail.overview.notes ?: "") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        onChangeImage(detail.overview.id, uri?.toString())
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    fun requestImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    LaunchedEffect(detail.overview.notes) {
        notes = detail.overview.notes ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail.overview.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                    IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = detail.overview.imageUri ?: com.example.gymapplktrack.R.drawable.ic_launcher_foreground,
                contentDescription = "Foto del ejercicio",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            )
            Button(onClick = ::requestImage) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cambiar foto")
            }
            RecordCard(detail)
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Notas", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Button(onClick = { onUpdateNotes(detail.overview.id, notes.ifBlank { null }) }) {
                        Text("Guardar notas")
                    }
                }
            }
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Historial de uso", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) { Text("Mes anterior") }
                        Text(currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() } + " ${currentMonth.year}")
                        TextButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) { Text("Siguiente mes") }
                    }
                    MonthCalendar(
                        month = currentMonth,
                        highlightedDays = detail.history.filter { it.month == currentMonth.month && it.year == currentMonth.year }
                            .map { it.dayOfMonth }
                            .toSet(),
                        onDayClick = { }
                    )
                    Button(onClick = { onViewHistory(detail.overview.id) }, modifier = Modifier.align(Alignment.End)) {
                        Text("Ver historial")
                    }
                }
            }
            if (detail.sets.isNotEmpty()) {
                Text("Últimas series", style = MaterialTheme.typography.titleMedium)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(detail.sets.take(5)) { set ->
                        SetHistoryItem(set)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar ejercicio") },
            text = { Text("Esta acción eliminará el ejercicio y su historial.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(detail.overview.id)
                    showDeleteDialog = false
                    onBack()
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }

    if (showEditDialog) {
        ExerciseEditDialog(
            detail = detail,
            onDismiss = { showEditDialog = false },
            onSave = { name, category, notesValue ->
                onUpdateDetails(detail.overview.id, name, category, notesValue)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun RecordCard(detail: ExerciseDetail) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Récord personal", style = MaterialTheme.typography.titleMedium)
            detail.overview.personalRecord?.let { record ->
                Text("${record.bestWeightKg} kg × ${record.bestReps}", style = MaterialTheme.typography.titleLarge)
                Text("Logrado por primera vez el ${record.firstAchievedDate}")
            } ?: Text("Aún sin primeras marcas")
        }
    }
}

@Composable
private fun SetHistoryItem(set: ExerciseSetHistory) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${set.date} — ${set.weightKg} kg × ${set.reps}", fontWeight = FontWeight.SemiBold)
            Text("Serie ${set.setIndex + 1}")
            set.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun ExerciseEditDialog(
    detail: ExerciseDetail,
    onDismiss: () -> Unit,
    onSave: (String, String?, String?) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(detail.overview.name) }
    var category by rememberSaveable { mutableStateOf(detail.overview.category ?: "") }
    var notes by rememberSaveable { mutableStateOf(detail.overview.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar ejercicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoría") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, category.takeIf { it.isNotBlank() }, notes.takeIf { it.isNotBlank() }) }) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
