package com.example.gymapplktrack.ui.navigation

sealed class BottomDestination(val route: String) {
    data object Exercises : BottomDestination("exercises")
    data object Routines : BottomDestination("routines")
    data object Profile : BottomDestination("profile")
}

object ExerciseRoutes {
    const val LIST = "exercises/list"
    const val ADD = "exercises/add"
    const val DETAIL = "exercises/detail/{exerciseId}"
    const val HISTORY = "exercises/history/{exerciseId}"
}

object RoutineRoutes {
    const val LIST = "routines/list"
    const val EDIT = "routines/edit?routineId={routineId}"
    const val WORKOUT = "routines/workout"
    const val SUMMARY = "routines/workout/summary"
}

object ProfileRoutes {
    const val HOME = "profile/home"
    const val PREFERENCES = "profile/preferences"
}
