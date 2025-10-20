package com.example.gymapplktrack

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gymapplktrack.data.local.AppDatabase
import com.example.gymapplktrack.data.local.entity.ExerciseEntity
import com.example.gymapplktrack.data.repository.GymRepository
import com.example.gymapplktrack.domain.model.WorkoutExercise
import com.example.gymapplktrack.domain.model.WorkoutInProgress
import com.example.gymapplktrack.domain.model.WorkoutSet
import com.google.gson.Gson
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class WorkoutFlowInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: GymRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = GymRepository(db, Gson())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun completeWorkoutUpdatesPersonalRecord() = runBlocking {
        val exerciseId = db.exerciseDao().insertExercise(ExerciseEntity(name = "Prensa"))
        val workout = WorkoutInProgress(
            routineId = null,
            startDate = LocalDate.now(),
            startTime = LocalTime.of(10, 0),
            exercises = listOf(WorkoutExercise(exerciseId, "Prensa", listOf(WorkoutSet(100f, 8))))
        )

        repository.saveActiveWorkout(workout)
        val summary = repository.completeWorkout(workout, LocalTime.of(11, 0), null)

        val record = db.exerciseDao().getPersonalRecord(exerciseId).first()
        assertEquals(100f, record?.bestWeightKg)
        assertEquals(1, summary.totalExercises)
    }
}
