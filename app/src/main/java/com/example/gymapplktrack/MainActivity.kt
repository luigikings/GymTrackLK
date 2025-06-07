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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import com.example.gymapplktrack.ui.theme.GymTrackTheme
import com.example.gymapplktrack.WeightProgressChart
import com.example.gymapplktrack.Routine
import com.example.gymapplktrack.RoutineRepository
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
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import android.net.Uri
import android.content.Intent
import com.example.gymapplktrack.ExerciseRepository
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import java.io.File
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.BLACK
        val repository = ExerciseRepository(this)
        val routineRepository = RoutineRepository(this)
        setContent {
            GymTrackTheme(darkTheme = true) {
                GymTrackApp(repository, routineRepository)
            }
        }
    }
}

@Composable
fun GymTrackApp(
    repository: ExerciseRepository,
    routineRepository: RoutineRepository
) {
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
                Screen.Routines -> RoutinesScreen(repository, routineRepository)
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
    var detailExercise by remember { mutableStateOf<Exercise?>(null) }
    val selectedExercises = remember { mutableStateListOf<Exercise>() }
    val exercises = remember {
        mutableStateListOf<Exercise>().apply {
            val saved = repository.loadExercises()
            if (saved.isNotEmpty()) {
                addAll(saved)
            }
        }
    }

    if (detailExercise != null) {
        ExerciseDetailScreen(
            exercise = detailExercise!!,
            onBack = { detailExercise = null; repository.saveExercises(exercises) },
            onAddRecord = { record ->
                detailExercise!!.records.add(record)
                repository.saveExercises(exercises)
            },
            onDeleteRecord = { record ->
                detailExercise!!.records.remove(record)
                repository.saveExercises(exercises)
            }
        )
    } else Column(modifier = Modifier.fillMaxSize()) {
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
                                } else {
                                    detailExercise = exercise
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
                                } else {
                                    detailExercise = exercise
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
                exercises.add(Exercise(name = name, imageUri = uri))
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
fun RoutinesScreen(
    exerciseRepository: ExerciseRepository,
    routineRepository: RoutineRepository
) {
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    var detailRoutine by remember { mutableStateOf<Routine?>(null) }
    val selectedRoutines = remember { mutableStateListOf<Routine>() }
    val routines = remember {
        mutableStateListOf<Routine>().apply { addAll(routineRepository.loadRoutines()) }
    }

    detailRoutine?.let { routine ->
        RoutineDetailScreen(
            routine = routine,
            exercises = exerciseRepository.loadExercises(),
            onBack = {
                detailRoutine = null
                routineRepository.saveRoutines(routines)
            },
            onAddExercise = {
                routine.exercises.add(it)
                routineRepository.saveRoutines(routines)
            },
            onRemoveExercise = {
                routine.exercises.remove(it)
                routineRepository.saveRoutines(routines)
            }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(routines) { routine ->
                    RoutineItem(
                        routine,
                        selected = routine in selectedRoutines,
                        onClick = {
                            if (selectionMode) {
                                if (routine in selectedRoutines) selectedRoutines.remove(routine) else selectedRoutines.add(routine)
                                selectionMode = selectedRoutines.isNotEmpty()
                            } else {
                                detailRoutine = routine
                            }
                        },
                        onLongClick = {
                            if (routine in selectedRoutines) selectedRoutines.remove(routine) else selectedRoutines.add(routine)
                            selectionMode = selectedRoutines.isNotEmpty()
                        }
                    )
                }
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
        ) { Text(stringResource(id = R.string.create_routine)) }
    }

    if (showDialog) {
        AddRoutineDialog(
            onDismiss = { showDialog = false },
            onAdd = { name ->
                routines.add(Routine(name))
                routineRepository.saveRoutines(routines)
                showDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    routines.removeAll(selectedRoutines)
                    routineRepository.saveRoutines(routines)
                    selectedRoutines.clear()
                    selectionMode = false
                    showDeleteDialog = false
                }) { Text(stringResource(id = R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(id = R.string.cancel)) } },
            text = { Text(stringResource(id = R.string.delete_routines_question, selectedRoutines.size)) }
        )
    }
}

@Composable
fun ProfileScreen() {
    Text(text = stringResource(id = R.string.profile))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoutineItem(
    routine: Routine,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp)
    ) {
        Text(text = routine.name, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterStart))
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

@Composable
fun AddRoutineDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onAdd(name) }) { Text(stringResource(id = R.string.add)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel)) } },
        title = { Text(stringResource(id = R.string.routine_name)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.routine_name)) }
            )
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RoutineDetailScreen(
    routine: Routine,
    exercises: List<Exercise>,
    onBack: () -> Unit,
    onAddExercise: (String) -> Unit,
    onRemoveExercise: (String) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var deleteExercise by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(routine.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                val routineExercises = exercises.filter { it.name in routine.exercises }
                items(routineExercises) { ex ->
                    ExerciseItem(
                        exercise = ex,
                        onLongClick = { deleteExercise = ex.name }
                    )
                }
            }
            Button(
                onClick = { showAdd = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { Text(stringResource(id = R.string.add_to_routine)) }
        }
    }

    if (showAdd) {
        AddExerciseToRoutineDialog(
            exercises = exercises.filter { it.name !in routine.exercises },
            onDismiss = { showAdd = false },
            onAdd = {
                onAddExercise(it)
                showAdd = false
            }
        )
    }

    deleteExercise?.let { name ->
        AlertDialog(
            onDismissRequest = { deleteExercise = null },
            confirmButton = {
                TextButton(onClick = {
                    onRemoveExercise(name)
                    deleteExercise = null
                }) { Text(stringResource(id = R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { deleteExercise = null }) { Text(stringResource(id = R.string.cancel)) } },
            text = { Text(stringResource(id = R.string.delete_from_routine_question)) }
        )
    }
}

@Composable
fun AddExerciseToRoutineDialog(
    exercises: List<Exercise>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var selected by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { selected?.let { onAdd(it) } }) { Text(stringResource(id = R.string.add)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel)) } },
        title = { Text(stringResource(id = R.string.add_exercise)) },
        text = {
            if (exercises.isEmpty()) {
                Text(stringResource(id = R.string.no_exercises_available))
            } else {
                LazyColumn {
                    items(exercises) { ex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selected = ex.name }
                                .background(if (selected == ex.name) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                .padding(8.dp)
                        ) {
                            Text(ex.name)
                        }
                    }
                }
            }
        }
    )
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
            val top = exercise.records.maxByOrNull { it.weight }
            if (top != null) {
                Text(text = "Record: ${top.weight}")
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
                val top = exercise.records.maxByOrNull { it.weight }
                if (top != null) {
                    Text(text = "Record: ${top.weight}")
                }
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddRecordDialog(onDismiss: () -> Unit, onSave: (Int, Int, String) -> Unit) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val w = weight.toIntOrNull()
                val r = reps.toIntOrNull()
                if (w != null && r != null && date.isNotBlank()) {
                    onSave(w, r, date)
                }
            }) { Text(text = stringResource(id = R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = stringResource(id = R.string.cancel)) } },
        title = { Text(text = stringResource(id = R.string.place_record)) },
        text = {
            Column {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(id = R.string.weight)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text(stringResource(id = R.string.reps)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.date)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                )
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        date = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    }
                    showDatePicker = false
                }) { Text(text = stringResource(id = R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(text = stringResource(id = R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExerciseDetailScreen(
    exercise: Exercise,
    onBack: () -> Unit,
    onAddRecord: (ExerciseRecord) -> Unit,
    onDeleteRecord: (ExerciseRecord) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var showRecord by remember { mutableStateOf<ExerciseRecord?>(null) }
    var deleteRecord by remember { mutableStateOf<ExerciseRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (exercise.imageUri != null) {
                AsyncImage(
                    model = exercise.imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(200.dp)
                )
            }
            val top = exercise.records.maxByOrNull { it.weight }
            Spacer(modifier = Modifier.height(8.dp))
            if (top != null) {
                Text(text = "Record: ${top.weight}")
                Text(text = "${top.reps} repeticiones", style = MaterialTheme.typography.bodySmall)
                Text(text = top.date, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { showAdd = true }) { Text(text = stringResource(id = R.string.place_record)) }
            if (exercise.records.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(id = R.string.record_history), fontWeight = FontWeight.Bold)
                exercise.records.sortedByDescending { it.weight }.forEach { rec ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(onClick = { showRecord = rec })
                    ) {
                        Text(text = "${rec.weight}kg, ${rec.reps}reps, ${rec.date}")
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { deleteRecord = rec }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(id = R.string.weight_progression), fontWeight = FontWeight.Bold)
                WeightProgressChart(exercise.records, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }

    if (showAdd) {
        AddRecordDialog(
            onDismiss = { showAdd = false },
            onSave = { w, r, d ->
                onAddRecord(ExerciseRecord(w, r, d))
                showAdd = false
            }
        )
    }

    showRecord?.let { rec ->
        AlertDialog(
            onDismissRequest = { showRecord = null },
            confirmButton = {
                TextButton(onClick = { showRecord = null }) { Text("OK") }
            },
            title = { Text(text = "Record") },
            text = {
                Column {
                    Text(text = "${rec.weight}kg")
                    Text(text = "${rec.reps} reps")
                    Text(text = rec.date)
                }
            }
        )
    }

    deleteRecord?.let { rec ->
        AlertDialog(
            onDismissRequest = { deleteRecord = null },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteRecord(rec)
                    deleteRecord = null
                }) { Text(text = stringResource(id = R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteRecord = null }) { Text(text = stringResource(id = R.string.cancel)) }
            },
            text = { Text(text = stringResource(id = R.string.delete_record_question)) }
        )
    }
}
