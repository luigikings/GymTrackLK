package com.example.gymapplktrack

import android.os.Bundle
import android.graphics.Color
import androidx.core.view.WindowCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import com.example.gymapplktrack.ui.theme.GymTrackTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import android.net.Uri
import android.content.Intent
import com.example.gymapplktrack.ExerciseRepository
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.BLACK
        val repository = ExerciseRepository(this)
        setContent {
            GymTrackTheme(darkTheme = true) {
                GymTrackApp(repository)
            }
        }
    }
}

@Composable
fun GymTrackApp(repository: ExerciseRepository) {
    var selectedScreen by remember { mutableStateOf(Screen.Exercises) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedScreen == Screen.Exercises,
                    onClick = { selectedScreen = Screen.Exercises },
                    label = { Text(stringResource(id = R.string.exercises)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null
                        )
                    }
                )
                NavigationBarItem(
                    selected = selectedScreen == Screen.Routines,
                    onClick = { selectedScreen = Screen.Routines },
                    label = { Text(stringResource(id = R.string.routines)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null
                        )
                    }
                )
                NavigationBarItem(
                    selected = selectedScreen == Screen.Profile,
                    onClick = { selectedScreen = Screen.Profile },
                    label = { Text(stringResource(id = R.string.profile)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            when (selectedScreen) {
                Screen.Exercises -> ExercisesScreen(repository)
                Screen.Routines -> RoutinesScreen()
                Screen.Profile -> ProfileScreen()
            }
        }
    }
}

enum class Screen { Exercises, Routines, Profile }

@Composable
fun ExercisesScreen(repository: ExerciseRepository) {
    var gridMode by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedExercises = remember { mutableStateListOf<Exercise>() }
    val exercises = remember {
        mutableStateListOf<Exercise>().apply {
            val saved = repository.loadExercises()
            if (saved.isNotEmpty()) {
                addAll(saved)
            } else {
                repeat(5) { add(Exercise(name = "Exercise ${it + 1}", record = "X")) }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text(stringResource(id = R.string.search_exercise)) }
        )

        val filtered = exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }

        Box(modifier = Modifier.weight(1f)) {
            if (gridMode) {
                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize()) {
                    items(filtered) { exercise ->
                        ExerciseItem(
                            exercise,
                            grid = true,
                            selected = exercise in selectedExercises,
                            onClick = {
                                if (selectionMode) {
                                    if (exercise in selectedExercises) selectedExercises.remove(exercise) else selectedExercises.add(exercise)
                                    selectionMode = selectedExercises.isNotEmpty()
                                }
                            },
                            onLongClick = {
                                if (exercise in selectedExercises) selectedExercises.remove(exercise) else selectedExercises.add(exercise)
                                selectionMode = selectedExercises.isNotEmpty()
                            }
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered) { exercise ->
                        ExerciseItem(
                            exercise,
                            selected = exercise in selectedExercises,
                            onClick = {
                                if (selectionMode) {
                                    if (exercise in selectedExercises) selectedExercises.remove(exercise) else selectedExercises.add(exercise)
                                    selectionMode = selectedExercises.isNotEmpty()
                                }
                            },
                            onLongClick = {
                                if (exercise in selectedExercises) selectedExercises.remove(exercise) else selectedExercises.add(exercise)
                                selectionMode = selectedExercises.isNotEmpty()
                            }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { gridMode = !gridMode },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                val icon: ImageVector = if (gridMode) Icons.Default.ViewList else Icons.Default.GridView
                Icon(imageVector = icon, contentDescription = null)
            }

            if (selectionMode) {
                FloatingActionButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }
        }

        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = stringResource(id = R.string.add_exercise))
        }
    }

    if (showDialog) {
        AddExerciseDialog(
            onDismiss = { showDialog = false },
            onAdd = { name, uri ->
                exercises.add(Exercise(name = name, record = "X", imageUri = uri))
                repository.saveExercises(exercises)
                showDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    exercises.removeAll(selectedExercises)
                    repository.saveExercises(exercises)
                    selectedExercises.clear()
                    selectionMode = false
                    showDeleteDialog = false
                }) {
                    Text(text = stringResource(id = R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            text = {
                Text(text = stringResource(id = R.string.delete_exercises_question, selectedExercises.size))
            }
        )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseItem(
    exercise: Exercise,
    grid: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val modifier = if (grid) {
        Modifier
            .padding(8.dp)
            .fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    }
    val contentModifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    if (grid) {
        Box(modifier = contentModifier) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            if (exercise.imageUri != null) {
                AsyncImage(
                    model = exercise.imageUri,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = exercise.name, fontWeight = FontWeight.Bold)
            Text(text = "Record: ${exercise.record}")
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    } else {
        Box(modifier = contentModifier) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = if (selected) 16.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            if (exercise.imageUri != null) {
                AsyncImage(
                    model = exercise.imageUri,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = exercise.name, fontWeight = FontWeight.Bold)
                Text(text = "Record: ${exercise.record}")
            }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddExerciseDialog(onDismiss: () -> Unit, onAdd: (String, Uri?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }
        }
        imageUri = it
    }

    fun saveImage(context: Context, uri: Uri): Uri? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.filesDir, "img_${System.currentTimeMillis()}" )
            file.outputStream().use { output ->
                input.copyTo(output)
            }
            Uri.fromFile(file)
        } catch (_: Exception) {
            null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    val storedUri = imageUri?.let { saveImage(context, it) }
                    onAdd(name, storedUri)
                }
            }) {
                Text(text = stringResource(id = R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        title = { Text(text = stringResource(id = R.string.exercise_name)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(id = R.string.exercise_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { launcher.launch(arrayOf("image/*")) }) {
                    Text(text = stringResource(id = R.string.select_photo))
                }
                if (imageUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            }
        }
    )
}
