package com.example.gymapplktrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.FitnessCenter
import com.example.gymapplktrack.ui.theme.GymTrackTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymTrackTheme {
                GymTrackApp()
            }
        }
    }
}

@Composable
fun GymTrackApp() {
    var selectedScreen by remember { mutableStateOf(Screen.Exercises) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedScreen == Screen.Exercises,
                    onClick = { selectedScreen = Screen.Exercises },
                    label = { Text(stringResource(id = R.string.exercises)) },
                    icon = {}
                )
                NavigationBarItem(
                    selected = selectedScreen == Screen.Routines,
                    onClick = { selectedScreen = Screen.Routines },
                    label = { Text(stringResource(id = R.string.routines)) },
                    icon = {}
                )
                NavigationBarItem(
                    selected = selectedScreen == Screen.Profile,
                    onClick = { selectedScreen = Screen.Profile },
                    label = { Text(stringResource(id = R.string.profile)) },
                    icon = {}
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (selectedScreen) {
                Screen.Exercises -> ExercisesScreen()
                Screen.Routines -> RoutinesScreen()
                Screen.Profile -> ProfileScreen()
            }
        }
    }
}

enum class Screen { Exercises, Routines, Profile }

@Composable
fun ExercisesScreen() {
    var gridMode by remember { mutableStateOf(false) }

    val exercises = remember {
        List(100) { index ->
            Exercise(name = "Exercise ${index + 1}", record = "X")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { gridMode = !gridMode }) {
                val icon: ImageVector = if (gridMode) Icons.Default.ViewList else Icons.Default.GridView
                Icon(imageVector = icon, contentDescription = null)
            }
        }

        if (gridMode) {
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize()) {
                items(exercises) { exercise ->
                    ExerciseItem(exercise, grid = true)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(exercises) { exercise ->
                    ExerciseItem(exercise)
                }
            }
        }
    }
}

@Composable
fun RoutinesScreen() {
    Text(text = stringResource(id = R.string.routines))
}

@Composable
fun ProfileScreen() {
    Text(text = stringResource(id = R.string.profile))
}

@Composable
fun ExerciseItem(exercise: Exercise, grid: Boolean = false) {
    val modifier = if (grid) {
        Modifier
            .padding(8.dp)
            .fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    }
    if (grid) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = exercise.name, fontWeight = FontWeight.Bold)
            Text(text = "Record: ${exercise.record}")
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = exercise.name, fontWeight = FontWeight.Bold)
                Text(text = "Record: ${exercise.record}")
            }
        }
    }
}
