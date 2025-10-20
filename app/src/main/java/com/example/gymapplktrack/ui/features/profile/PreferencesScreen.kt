package com.example.gymapplktrack.ui.features.profile

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymapplktrack.domain.model.StreakMode
import com.example.gymapplktrack.domain.model.ThemePreference
import com.example.gymapplktrack.domain.model.WeightUnit
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    state: ProfileUiState,
    onBack: () -> Unit,
    onThemeChange: (ThemePreference) -> Unit,
    onUnitChange: (WeightUnit) -> Unit,
    onStreakModeChange: (StreakMode) -> Unit,
    onCreatineReminderChange: (Boolean, LocalTime?) -> Unit
) {
    val preferences = state.preferences ?: return
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferencias") },
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
            Text("Tema", style = MaterialTheme.typography.titleMedium)
            ThemePreference.values().forEach { theme ->
                PreferenceRadioRow(
                    text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = preferences.theme == theme,
                    onSelect = { onThemeChange(theme) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Unidades", style = MaterialTheme.typography.titleMedium)
            WeightUnit.values().forEach { unit ->
                PreferenceRadioRow(
                    text = unit.name,
                    selected = preferences.weightUnit == unit,
                    onSelect = { onUnitChange(unit) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Modo de racha", style = MaterialTheme.typography.titleMedium)
            StreakMode.values().forEach { mode ->
                PreferenceRadioRow(
                    text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = preferences.streakMode == mode,
                    onSelect = { onStreakModeChange(mode) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val reminderEnabled = remember { mutableStateOf(preferences.creatineReminderEnabled) }
            val reminderTime = remember { mutableStateOf(preferences.creatineReminderTime?.toString() ?: "08:00") }
            Text("Recordatorio de creatina", style = MaterialTheme.typography.titleMedium)
            PreferenceRadioRow(
                text = if (reminderEnabled.value) "Activado" else "Desactivado",
                selected = reminderEnabled.value,
                onSelect = { reminderEnabled.value = !reminderEnabled.value }
            )
            OutlinedTextField(
                value = reminderTime.value,
                onValueChange = { reminderTime.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Hora (HH:MM)") },
                enabled = reminderEnabled.value
            )
            Button(onClick = {
                val time = runCatching { LocalTime.parse(reminderTime.value) }.getOrNull()
                onCreatineReminderChange(reminderEnabled.value, time)
            }) { Text("Guardar recordatorio") }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Cerrar") }
        }
    }
}

@Composable
private fun PreferenceRadioRow(text: String, selected: Boolean, onSelect: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text, modifier = Modifier.padding(start = 8.dp))
    }
}
