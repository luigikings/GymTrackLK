package com.example.gymapplktrack.ui.features.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gymapplktrack.domain.model.DailyLog
import com.example.gymapplktrack.ui.components.MonthCalendar
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToggleCreatine: (LocalDate) -> Unit,
    onEnsureLog: (LocalDate) -> Unit,
    onOpenPreferences: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Perfil", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    TextButton(onClick = onOpenPreferences) { Text("Preferencias") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Calendario de entrenos", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = onPreviousMonth) { Text("Anterior") }
                            Text("${state.month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${state.month.year}")
                            TextButton(onClick = onNextMonth) { Text("Siguiente") }
                        }
                        MonthCalendar(
                            month = state.month,
                            highlightedDays = state.workoutMarkedDays,
                            onDayClick = onEnsureLog
                        )
                    }
                }
            }
            item {
                Surface(
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Creatina", style = MaterialTheme.typography.titleMedium)
                        state.logs.forEach { log ->
                            CreatineRow(log = log, onToggle = onToggleCreatine)
                        }
                    }
                }
            }
            item {
                Surface(
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Racha actual", style = MaterialTheme.typography.titleMedium)
                        Text("${state.streakInfo?.currentStreak ?: 0} ${state.streakInfo?.mode?.name?.lowercase()} consecutivos")
                    }
                }
            }
            item {
                Surface(
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Estadísticas", style = MaterialTheme.typography.titleMedium)
                        Text("Sesiones este mes: ${state.summary?.monthlySessions ?: 0}")
                        Text("Ejercicios más frecuentes: ${state.summary?.topExercises?.joinToString().orEmpty()}")
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatineRow(log: DailyLog, onToggle: (LocalDate) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${log.date}")
        Checkbox(checked = log.tookCreatine, onCheckedChange = { onToggle(log.date) })
    }
}
