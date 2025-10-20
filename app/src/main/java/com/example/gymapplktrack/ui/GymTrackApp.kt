package com.example.gymapplktrack.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Surface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymapplktrack.LocalAppContainer
import com.example.gymapplktrack.domain.model.ExerciseSort
import com.example.gymapplktrack.ui.theme.gymGradientBackground
import com.example.gymapplktrack.ui.features.exercises.AddExerciseScreen
import com.example.gymapplktrack.ui.features.exercises.ExerciseDetailScreen
import com.example.gymapplktrack.ui.features.exercises.ExerciseDetailViewModel
import com.example.gymapplktrack.ui.features.exercises.ExerciseHistoryScreen
import com.example.gymapplktrack.ui.features.exercises.ExercisesScreen
import com.example.gymapplktrack.ui.features.exercises.ExercisesViewModel
import com.example.gymapplktrack.ui.features.profile.PreferencesScreen
import com.example.gymapplktrack.ui.features.profile.ProfileScreen
import com.example.gymapplktrack.ui.features.profile.ProfileViewModel
import com.example.gymapplktrack.ui.features.routines.RoutineEditorScreen
import com.example.gymapplktrack.ui.features.routines.RoutineEditorViewModel
import com.example.gymapplktrack.ui.features.routines.RoutinesScreen
import com.example.gymapplktrack.ui.features.routines.RoutinesViewModel
import com.example.gymapplktrack.ui.features.workout.WorkoutScreen
import com.example.gymapplktrack.ui.features.workout.WorkoutSummaryScreen
import com.example.gymapplktrack.ui.features.workout.WorkoutViewModel
import com.example.gymapplktrack.ui.navigation.BottomDestination
import com.example.gymapplktrack.ui.navigation.ExerciseRoutes
import com.example.gymapplktrack.ui.navigation.ProfileRoutes
import com.example.gymapplktrack.ui.navigation.RoutineRoutes

@Composable
fun GymTrackApp() {
    val container = LocalAppContainer.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current

    val bottomDestinations = listOf(
        BottomDestination.Exercises,
        BottomDestination.Routines,
        BottomDestination.Profile
    )

    Surface(color = Color.Transparent) {
        Box(modifier = Modifier.fillMaxSize().gymGradientBackground()) {
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        bottomDestinations.forEach { destination ->
                            val selected = backStackEntry?.destination?.hierarchy?.any { it.route == destination.route } == true
                            val icon = when (destination) {
                                BottomDestination.Exercises -> Icons.Filled.FitnessCenter
                                BottomDestination.Routines -> Icons.Filled.ListAlt
                                BottomDestination.Profile -> Icons.Filled.AccountCircle
                            }
                            val label = when (destination) {
                                BottomDestination.Exercises -> "Ejercicios"
                                BottomDestination.Routines -> "Rutinas"
                                BottomDestination.Profile -> "Perfil"
                            }
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = BottomDestination.Exercises.route,
                    modifier = Modifier.padding(padding)
                ) {
                    navigation(startDestination = ExerciseRoutes.LIST, route = BottomDestination.Exercises.route) {
                        composable(ExerciseRoutes.LIST) {
                            val viewModel: ExercisesViewModel = viewModel(factory = ExercisesViewModel.provideFactory(container))
                            val state by viewModel.uiState.collectAsStateWithLifecycle()
                            ExercisesScreen(
                                state = state,
                                events = viewModel.events,
                                onSearchChange = viewModel::onSearchChange,
                                onSortChange = viewModel::onSortSelected,
                                onCategoryChange = viewModel::onCategorySelected,
                                onAddExercise = { navController.navigate(ExerciseRoutes.ADD) },
                                onExerciseClick = { id -> navController.navigate("exercises/detail/$id") },
                                onDeleteExercise = viewModel::deleteExercise
                            )
                        }
                        composable(ExerciseRoutes.ADD) {
                            val viewModel: ExercisesViewModel = viewModel(factory = ExercisesViewModel.provideFactory(container))
                            AddExerciseScreen(
                                onBack = { navController.popBackStack() },
                                onSave = { name, category, image, notes ->
                                    viewModel.createExercise(name, category, image, notes)
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(
                            route = ExerciseRoutes.DETAIL,
                            arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
                        ) {
                            val detailViewModel: ExerciseDetailViewModel = viewModel(factory = ExerciseDetailViewModel.provideFactory(container))
                            val listViewModel: ExercisesViewModel = viewModel(factory = ExercisesViewModel.provideFactory(container))
                            val state by detailViewModel.uiState.collectAsStateWithLifecycle()
                            ExerciseDetailScreen(
                                state = state,
                                onBack = { navController.popBackStack() },
                                onEdit = {},
                                onDelete = listViewModel::deleteExercise,
                                onChangeImage = listViewModel::updateImage,
                                onUpdateNotes = listViewModel::updateNotes,
                                onViewHistory = { navController.navigate("exercises/history/$it") },
                                onUpdateDetails = { id, name, category, notes ->
                                    state.detail?.overview?.takeIf { it.id == id }?.let { overview ->
                                        listViewModel.updateExerciseDetails(overview, name, category, notes)
                                    }
                                }
                            )
                        }
                        composable(
                            route = ExerciseRoutes.HISTORY,
                            arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
                        ) {
                            val detailViewModel: ExerciseDetailViewModel = viewModel(factory = ExerciseDetailViewModel.provideFactory(container))
                            val state by detailViewModel.uiState.collectAsStateWithLifecycle()
                            ExerciseHistoryScreen(state = state, onBack = { navController.popBackStack() })
                        }
                    }
                    navigation(startDestination = RoutineRoutes.LIST, route = BottomDestination.Routines.route) {
                        composable(RoutineRoutes.LIST) {
                            val routinesViewModel: RoutinesViewModel = viewModel(factory = RoutinesViewModel.provideFactory(container))
                            val routinesState by routinesViewModel.uiState.collectAsStateWithLifecycle()
                            val parentEntry = remember(navController) { navController.getBackStackEntry(BottomDestination.Routines.route) }
                            val workoutViewModel: WorkoutViewModel = viewModel(parentEntry, factory = WorkoutViewModel.provideFactory(container))
                            val workoutState by workoutViewModel.uiState.collectAsStateWithLifecycle()
                            RoutinesScreen(
                                state = routinesState,
                                workoutState = workoutState,
                                onCreateRoutine = { navController.navigate("routines/edit") },
                                onStartRoutine = {
                                    workoutViewModel.startRoutine(it)
                                    navController.navigate(RoutineRoutes.WORKOUT)
                                },
                                onStartFree = {
                                    workoutViewModel.startRoutine(null)
                                    navController.navigate(RoutineRoutes.WORKOUT)
                                },
                                onEditRoutine = { id -> navController.navigate("routines/edit?routineId=$id") },
                                onDeleteRoutine = routinesViewModel::deleteRoutine,
                                onResumeWorkout = { navController.navigate(RoutineRoutes.WORKOUT) }
                            )
                        }
                        composable(
                            route = RoutineRoutes.EDIT,
                            arguments = listOf(navArgument("routineId") {
                                type = NavType.LongType
                                defaultValue = -1
                            })
                        ) {
                            val editorViewModel: RoutineEditorViewModel = viewModel(factory = RoutineEditorViewModel.provideFactory(container))
                            val editorState by editorViewModel.uiState.collectAsStateWithLifecycle()
                            RoutineEditorScreen(
                                state = editorState,
                                events = editorViewModel.events,
                                onBack = { navController.popBackStack() },
                                onNameChange = editorViewModel::updateName,
                                onAddExercise = editorViewModel::addExercise,
                                onRemoveExercise = editorViewModel::removeExercise,
                                onMoveExercise = editorViewModel::reorderExercise,
                                onSave = editorViewModel::saveRoutine,
                                onSaved = { navController.popBackStack() }
                            )
                        }
                        composable(RoutineRoutes.WORKOUT) {
                            val parentEntry = remember(navController) { navController.getBackStackEntry(BottomDestination.Routines.route) }
                            val workoutViewModel: WorkoutViewModel = viewModel(parentEntry, factory = WorkoutViewModel.provideFactory(container))
                            val workoutState by workoutViewModel.uiState.collectAsStateWithLifecycle()
                            WorkoutScreen(
                                state = workoutState,
                                events = workoutViewModel.events,
                                onBack = { navController.popBackStack() },
                                onAddExercise = workoutViewModel::addExerciseToWorkout,
                                onRemoveExercise = workoutViewModel::removeExercise,
                                onAddSet = workoutViewModel::addSet,
                                onRemoveSet = workoutViewModel::removeSet,
                                onFinish = workoutViewModel::finishWorkout,
                                onDiscard = {
                                    workoutViewModel.discardWorkout()
                                    navController.popBackStack()
                                },
                                onNotesChange = workoutViewModel::setNotes,
                                onShowSummary = {
                                    navController.navigate(RoutineRoutes.SUMMARY)
                                }
                            )
                        }
                        composable(RoutineRoutes.SUMMARY) {
                            val parentEntry = remember(navController) { navController.getBackStackEntry(BottomDestination.Routines.route) }
                            val workoutViewModel: WorkoutViewModel = viewModel(parentEntry, factory = WorkoutViewModel.provideFactory(container))
                            val workoutState by workoutViewModel.uiState.collectAsStateWithLifecycle()
                            WorkoutSummaryScreen(
                                state = workoutState,
                                events = workoutViewModel.events,
                                onBack = {
                                    workoutViewModel.clearSummary()
                                    navController.popBackStack(BottomDestination.Routines.route, inclusive = false)
                                },
                                onShare = { summary ->
                                    val shareText = "Entreno completado: ${summary.totalExercises} ejercicios, ${summary.totalSets} series"
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartir resumen"))
                                },
                                onSaveRoutine = workoutViewModel::saveCompletedWorkoutAsRoutine
                            )
                        }
                    }
                    navigation(startDestination = ProfileRoutes.HOME, route = BottomDestination.Profile.route) {
                        composable(ProfileRoutes.HOME) {
                            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.provideFactory(container))
                            val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
                            ProfileScreen(
                                state = profileState,
                                onPreviousMonth = profileViewModel::previousMonth,
                                onNextMonth = profileViewModel::nextMonth,
                                onToggleCreatine = profileViewModel::toggleCreatine,
                                onEnsureLog = profileViewModel::ensureWorkoutLog,
                                onOpenPreferences = { navController.navigate(ProfileRoutes.PREFERENCES) }
                            )
                        }
                        composable(ProfileRoutes.PREFERENCES) {
                            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.provideFactory(container))
                            val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
                            PreferencesScreen(
                                state = profileState,
                                onBack = { navController.popBackStack() },
                                onThemeChange = profileViewModel::updateTheme,
                                onUnitChange = profileViewModel::updateUnit,
                                onStreakModeChange = profileViewModel::updateStreakMode,
                                onCreatineReminderChange = profileViewModel::updateCreatineReminder
                            )
                        }
                    }
                }
            }
        }
    }
}
