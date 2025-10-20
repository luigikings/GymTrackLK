package com.example.gymapplktrack

import android.content.Context
import com.example.gymapplktrack.data.local.AppDatabase
import com.example.gymapplktrack.data.local.entity.DailyLogEntity
import com.example.gymapplktrack.data.local.entity.ExerciseEntity
import com.example.gymapplktrack.data.local.entity.RoutineEntity
import com.example.gymapplktrack.data.local.entity.RoutineExerciseEntity
import com.example.gymapplktrack.data.repository.GymRepository
import com.example.gymapplktrack.data.repository.PreferencesRepository
import com.google.gson.Gson
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.build(context)
    val repository = GymRepository(database, Gson())
    val preferencesRepository = PreferencesRepository(context)

    init {
        seedIfNecessary()
    }

    private fun seedIfNecessary() {
        CoroutineScope(Dispatchers.IO).launch {
            val exerciseDao = database.exerciseDao()
            val existing = exerciseDao.getExercisesWithRecord().firstOrNull()
            if (existing.isNullOrEmpty()) {
                val exercises = listOf(
                    "Press banca",
                    "Sentadilla",
                    "Peso muerto",
                    "Dominadas",
                    "Remo con barra",
                    "Press militar",
                    "Jalón al pecho",
                    "Curl bíceps",
                    "Extensión de tríceps",
                    "Elevación lateral",
                    "Hip thrust"
                )
                val ids = exercises.map { name ->
                    exerciseDao.insertExercise(ExerciseEntity(name = name))
                }
                val routineDao = database.routineDao()
                val routineId = routineDao.insertRoutine(
                    RoutineEntity(name = "Rutina ejemplo", createdAt = LocalDateTime.now())
                )
                routineDao.insertRoutineExercises(
                    ids.take(5).mapIndexed { index, exerciseId ->
                        RoutineExerciseEntity(
                            routineId = routineId,
                            exerciseId = exerciseId,
                            order = index
                        )
                    }
                )
                val dailyLogDao = database.dailyLogDao()
                repeat(5) { offset ->
                    val date = LocalDate.now().minusDays(offset.toLong())
                    dailyLogDao.upsertLog(
                        DailyLogEntity(
                            date = date,
                            didWorkout = offset % 2 == 0,
                            tookCreatine = offset % 3 != 0
                        )
                    )
                }
            }
        }
    }
}

private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.firstOrNull(): T? = try {
    first()
} catch (e: NoSuchElementException) {
    null
}
