package com.example.gymapplktrack.ui.features.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymapplktrack.domain.model.WorkoutSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummaryScreen(state: WorkoutUiState, onBack: () -> Unit, onShare: (WorkoutSummary) -> Unit) {
    val summary = state.lastSummary
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entreno completado") },
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
            if (summary == null) {
                Text("No hay resumen disponible", style = MaterialTheme.typography.titleMedium)
            } else {
                Text("¡Buen trabajo!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Duración: ${summary.duration.toMinutes()} minutos")
                Text("Ejercicios: ${summary.totalExercises}")
                Text("Series totales: ${summary.totalSets}")
                Spacer(modifier = Modifier.height(12.dp))
                if (summary.brokenRecords.isNotEmpty()) {
                    Text("Récords alcanzados", fontWeight = FontWeight.SemiBold)
                    summary.brokenRecords.forEach { record ->
                        Text("${record.exerciseName}: ${record.weightKg} kg × ${record.reps}")
                    }
                } else {
                    Text("Sin nuevos récords esta vez", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onShare(summary) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Compartir resumen")
                }
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver")
                }
            }
        }
    }
}
