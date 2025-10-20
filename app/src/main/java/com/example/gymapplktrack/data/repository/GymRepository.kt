package com.example.gymapplktrack.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.gymapplktrack.data.local.AppDatabase
import com.example.gymapplktrack.data.local.dao.ExerciseUsageCount
import com.example.gymapplktrack.data.local.dao.WorkoutSetWithSession
import com.example.gymapplktrack.data.local.entity.ActiveWorkoutEntity
import com.example.gymapplktrack.data.local.entity.ActiveWorkoutExercise
import com.example.gymapplktrack.data.local.entity.ActiveWorkoutPayload
import com.example.gymapplktrack.data.local.entity.ActiveWorkoutSet
import com.example.gymapplktrack.data.local.entity.DailyLogEntity
import com.example.gymapplktrack.data.local.entity.ExerciseEntity
import com.example.gymapplktrack.data.local.entity.ExerciseHistoryEntity
import com.example.gymapplktrack.data.local.entity.PersonalRecordEntity
import com.example.gymapplktrack.data.local.entity.RoutineEntity
import com.example.gymapplktrack.data.local.entity.RoutineExerciseEntity
import com.example.gymapplktrack.data.local.entity.SessionWithSets
import com.example.gymapplktrack.data.local.entity.WorkoutSessionEntity
import com.example.gymapplktrack.data.local.entity.WorkoutSetEntity
import com.example.gymapplktrack.domain.model.ActivitySummary
import com.example.gymapplktrack.domain.model.DailyLog
import com.example.gymapplktrack.domain.model.ExerciseDetail
import com.example.gymapplktrack.domain.model.ExerciseOverview
import com.example.gymapplktrack.domain.model.ExerciseSetHistory
import com.example.gymapplktrack.domain.model.PersonalRecord
import com.example.gymapplktrack.domain.model.PersonalRecordBreak
import com.example.gymapplktrack.domain.model.RoutineExerciseItem
import com.example.gymapplktrack.domain.model.RoutineOverview
import com.example.gymapplktrack.domain.model.StreakMode
import com.example.gymapplktrack.domain.model.StreakInfo
import com.example.gymapplktrack.domain.model.WorkoutExercise
import com.example.gymapplktrack.domain.model.WorkoutInProgress
import com.example.gymapplktrack.domain.model.WorkoutSet
import com.example.gymapplktrack.domain.model.WorkoutSummary
import com.example.gymapplktrack.domain.util.PersonalRecordCalculator
import com.example.gymapplktrack.domain.util.StreakCalculator
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GymRepository(
    private val database: AppDatabase,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val exerciseDao = database.exerciseDao()
    private val workoutDao = database.workoutDao()
    private val routineDao = database.routineDao()
    private val dailyLogDao = database.dailyLogDao()
    private val activeWorkoutDao = database.activeWorkoutDao()

    fun observeExercises(): Flow<List<ExerciseOverview>> {
        val exercisesFlow = exerciseDao.getExercisesWithRecord()
        val usageFlow = exerciseDao.getExerciseUsageCounts()
        return combine(exercisesFlow, usageFlow) { exercises, usage ->
            mapToExerciseOverview(exercises, usage)
        }
    }

    fun observeExerciseDetail(exerciseId: Long): Flow<ExerciseDetail?> {
        val overviewFlow = observeExercises().map { list -> list.find { it.id == exerciseId } }
        val historyFlow = exerciseDao.getExerciseHistory(exerciseId)
        val setsFlow = workoutDao.getSetsForExercise(exerciseId)
        return combine(overviewFlow, historyFlow, setsFlow) { overview, history, sets ->
            overview?.let {
                ExerciseDetail(
                    overview = it,
                    history = history.map { entry -> entry.date }.sortedDescending(),
                    sets = sets.map { set -> set.toDomain() }
                )
            }
        }
    }

    suspend fun insertExercise(
        name: String,
        category: String?,
        imageUri: String?,
        notes: String?
    ): Long = withContext(dispatcher) {
        exerciseDao.insertExercise(
            ExerciseEntity(
                name = name,
                category = category,
                imageUri = imageUri,
                notes = notes
            )
        )
    }

    suspend fun updateExercise(overview: ExerciseOverview): Unit = withContext(dispatcher) {
        exerciseDao.updateExercise(
            ExerciseEntity(
                id = overview.id,
                name = overview.name,
                category = overview.category,
                imageUri = overview.imageUri,
                notes = overview.notes
            )
        )
    }

    suspend fun updateExerciseNotes(exerciseId: Long, notes: String?): Unit = withContext(dispatcher) {
        val entity = exerciseDao.getExerciseWithRecord(exerciseId).first()?.exercise ?: return@withContext
        exerciseDao.updateExercise(entity.copy(notes = notes))
    }

    suspend fun updateExerciseImage(exerciseId: Long, imageUri: String?): Unit = withContext(dispatcher) {
        val entity = exerciseDao.getExerciseWithRecord(exerciseId).first()?.exercise ?: return@withContext
        exerciseDao.updateExercise(entity.copy(imageUri = imageUri))
    }

    suspend fun deleteExercise(exerciseId: Long): Unit = withContext(dispatcher) {
        val entity = exerciseDao.getExerciseWithRecord(exerciseId).first()?.exercise ?: return@withContext
        exerciseDao.deleteExercise(entity)
    }

    fun observeCategories(): Flow<List<String>> = exerciseDao.getCategories()

    fun observeRoutines(): Flow<List<RoutineOverview>> {
        val routinesFlow = routineDao.getRoutines()
        val exercisesFlow = observeExercises()
        return combine(routinesFlow, exercisesFlow) { routines, exercises ->
            routines.map { routine ->
                RoutineOverview(
                    id = routine.routine.id,
                    name = routine.routine.name,
                    createdAt = routine.routine.createdAt,
                    exercises = routine.exercises.map { routineExercise ->
                        val exercise = exercises.find { it.id == routineExercise.exerciseId }
                        RoutineExerciseItem(
                            id = routineExercise.id,
                            exerciseId = routineExercise.exerciseId,
                            name = exercise?.name ?: "Ejercicio",
                            order = routineExercise.order
                        )
                    }.sortedBy { it.order }
                )
            }
        }
    }

    fun observeRoutine(id: Long): Flow<RoutineOverview?> {
        return observeRoutines().map { routines -> routines.find { it.id == id } }
    }

    suspend fun upsertRoutine(
        routineId: Long?,
        name: String,
        exerciseOrder: List<Long>
    ): Long = withContext(dispatcher) {
        val id = if (routineId == null) {
            routineDao.insertRoutine(
                RoutineEntity(name = name, createdAt = LocalDateTime.now())
            )
        } else {
            routineDao.updateRoutine(
                RoutineEntity(id = routineId, name = name, createdAt = LocalDateTime.now())
            )
            routineDao.deleteRoutineExercises(routineId)
            routineId
        }
        val routineExercises = exerciseOrder.mapIndexed { index, exerciseId ->
            RoutineExerciseEntity(
                routineId = id,
                exerciseId = exerciseId,
                order = index
            )
        }
        routineDao.insertRoutineExercises(routineExercises)
        id
    }

    suspend fun deleteRoutine(routineId: Long) = withContext(dispatcher) {
        routineDao.deleteRoutineExercises(routineId)
        routineDao.deleteRoutine(routineId)
    }

    fun observeActiveWorkout(): Flow<WorkoutInProgress?> {
        val exercisesFlow = observeExercises()
        return combine(activeWorkoutDao.observeActiveWorkout(), exercisesFlow) { active, exercises ->
            active?.let {
                val items = it.payload.exercises.map { payload ->
                    val name = exercises.find { ex -> ex.id == payload.exerciseId }?.name ?: payload.name
                    WorkoutExercise(
                        exerciseId = payload.exerciseId,
                        name = name,
                        sets = payload.sets.map { set -> WorkoutSet(set.weightKg, set.reps) }
                    )
                }
                WorkoutInProgress(
                    routineId = it.routineId,
                    startDate = it.startDate,
                    startTime = it.startTime,
                    exercises = items
                )
            }
        }
    }

    suspend fun saveActiveWorkout(state: WorkoutInProgress) = withContext(dispatcher) {
        val payload = ActiveWorkoutPayload(
            exercises = state.exercises.map { exercise ->
                ActiveWorkoutExercise(
                    exerciseId = exercise.exerciseId,
                    name = exercise.name,
                    sets = exercise.sets.map { set -> ActiveWorkoutSet(set.weightKg, set.reps) }
                )
            }
        )
        activeWorkoutDao.upsertActiveWorkout(
            ActiveWorkoutEntity(
                id = 0,
                routineId = state.routineId,
                startDate = state.startDate,
                startTime = state.startTime,
                payload = payload
            )
        )
    }

    suspend fun clearActiveWorkout() = withContext(dispatcher) {
        activeWorkoutDao.clear()
    }

    suspend fun completeWorkout(
        state: WorkoutInProgress,
        endTime: LocalTime,
        notes: String?
    ): WorkoutSummary = withContext(dispatcher) {
        val sessionId = workoutDao.insertSession(
            WorkoutSessionEntity(
                date = state.startDate,
                startTime = state.startTime,
                endTime = endTime,
                notes = notes
            )
        )
        val sets = mutableListOf<WorkoutSetEntity>()
        state.exercises.forEach { exercise ->
            exercise.sets.forEachIndexed { index, set ->
                sets += WorkoutSetEntity(
                    sessionId = sessionId,
                    exerciseId = exercise.exerciseId,
                    setIndex = index,
                    weightKg = set.weightKg,
                    reps = set.reps
                )
            }
        }
        workoutDao.insertSets(sets)

        // update daily log
        val log = DailyLogEntity(
            date = state.startDate,
            didWorkout = true,
            tookCreatine = dailyLogDao.getLogs().first().find { it.date == state.startDate }?.tookCreatine ?: false
        )
        dailyLogDao.upsertLog(log)

        // exercise history
        val historyEntries = state.exercises.map { exercise ->
            ExerciseHistoryEntity(
                exerciseId = exercise.exerciseId,
                date = state.startDate
            )
        }
        exerciseDao.insertExerciseHistory(historyEntries)

        val brokenRecords = updatePersonalRecords(state)

        clearActiveWorkout()

        WorkoutSummary(
            sessionId = sessionId,
            duration = Duration.between(state.startTime, endTime),
            totalExercises = state.exercises.count { it.sets.isNotEmpty() },
            totalSets = state.exercises.sumOf { it.sets.size },
            brokenRecords = brokenRecords
        )
    }

    private suspend fun updatePersonalRecords(state: WorkoutInProgress): List<PersonalRecordBreak> {
        val broken = mutableListOf<PersonalRecordBreak>()
        val historyMap = mutableMapOf<Long, PersonalRecordEntity?>()
        state.exercises.forEach { exercise ->
            val candidates = exercise.sets.map { set ->
                PersonalRecordCalculator.Candidate(
                    weightKg = set.weightKg,
                    reps = set.reps,
                    date = state.startDate
                )
            }
            val best = PersonalRecordCalculator.selectBest(candidates) ?: return@forEach
            val current = historyMap.getOrPut(exercise.exerciseId) {
                exerciseDao.getPersonalRecord(exercise.exerciseId).first()
            }
            val shouldUpdate = current == null ||
                PersonalRecordCalculator.compare(best, current.toCandidate()) > 0
            if (shouldUpdate) {
                exerciseDao.upsertPersonalRecord(
                    PersonalRecordEntity(
                        exerciseId = exercise.exerciseId,
                        bestWeightKg = best.weightKg,
                        bestReps = best.reps,
                        firstAchievedDate = if (current == null) {
                            best.date
                        } else if (PersonalRecordCalculator.compare(best, current.toCandidate()) > 0) {
                            best.date
                        } else {
                            current.firstAchievedDate
                        }
                    )
                )
                broken += PersonalRecordBreak(
                    exerciseName = exercise.name,
                    weightKg = best.weightKg,
                    reps = best.reps
                )
            }
        }
        return broken
    }

    fun observeDailyLogs(): Flow<List<DailyLog>> = dailyLogDao.getLogs().map { list ->
        list.map { DailyLog(it.date, it.didWorkout, it.tookCreatine) }
    }

    fun observeProfileSummary(month: YearMonth): Flow<ActivitySummary> {
        val sessionsFlow = workoutDao.getSessions()
        val exercisesFlow = observeExercises()
        val recordsFlow = exerciseDao.getExercisesWithRecord()
        return combine(sessionsFlow, exercisesFlow, recordsFlow) { sessions, exercises, records ->
            val monthSessions = sessions.filter { it.session.date.year == month.year && it.session.date.month == month.month }
            val topExercises = sessions
                .flatMap { it.sets }
                .groupBy { it.exerciseId }
                .mapValues { it.value.size }
                .entries
                .sortedByDescending { it.value }
                .take(3)
                .mapNotNull { entry -> exercises.find { it.id == entry.key }?.name }
            val recentRecords = records.mapNotNull { record ->
                record.record?.let {
                    "${record.exercise.name}: ${it.bestWeightKg} kg x ${it.bestReps}"
                }
            }
            ActivitySummary(
                monthlySessions = monthSessions.size,
                topExercises = topExercises,
                recentPersonalRecords = recentRecords
            )
        }
    }

    fun observeStreak(mode: StreakMode): Flow<StreakInfo> {
        return observeDailyLogs().map { logs ->
            val streak = StreakCalculator.calculate(logs, mode)
            StreakInfo(streak, mode)
        }
    }

    suspend fun toggleCreatine(date: LocalDate, tookCreatine: Boolean) = withContext(dispatcher) {
        dailyLogDao.upsertLog(
            DailyLogEntity(
                date = date,
                didWorkout = dailyLogDao.getLogs().first().find { it.date == date }?.didWorkout ?: false,
                tookCreatine = tookCreatine
            )
        )
    }

    suspend fun ensureWorkoutLog(date: LocalDate) = withContext(dispatcher) {
        val existing = dailyLogDao.getLogs().first().find { it.date == date }
        if (existing == null) {
            dailyLogDao.upsertLog(DailyLogEntity(date = date, didWorkout = false, tookCreatine = false))
        }
    }

    suspend fun exportBackup(resolver: ContentResolver, uri: Uri) = withContext(dispatcher) {
        val payload = collectBackup()
        resolver.openOutputStream(uri)?.use { stream ->
            OutputStreamWriter(stream).use { writer ->
                writer.write(gson.toJson(payload))
            }
        }
    }

    suspend fun importBackup(resolver: ContentResolver, uri: Uri) = withContext(dispatcher) {
        val text = resolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).use { reader ->
                reader.readText()
            }
        } ?: return@withContext
        val payload = gson.fromJson(text, BackupPayload::class.java)
        database.clearAllTables()
        database.exerciseDao().apply {
            payload.exercises.forEach { insertExercise(it.toEntity()) }
            payload.personalRecords.forEach { upsertPersonalRecord(it.toEntity()) }
            insertExerciseHistory(payload.exerciseHistory.map { it.toEntity() })
        }
        database.routineDao().apply {
            payload.routines.forEach { insertRoutine(it.toEntity()) }
            insertRoutineExercises(payload.routineExercises.map { it.toEntity() })
        }
        database.workoutDao().apply {
            payload.sessions.forEach { insertSession(it.toEntity()) }
            insertSets(payload.sets.map { it.toEntity() })
        }
        database.dailyLogDao().apply {
            payload.dailyLogs.forEach { upsertLog(it.toEntity()) }
        }
    }

    private suspend fun collectBackup(): BackupPayload {
        val exercises = exerciseDao.getExercisesWithRecord().first()
        val usage = exerciseDao.getExerciseUsageCounts().first()
        val sessions = workoutDao.getSessions().first()
        val logs = dailyLogDao.getLogs().first()
        val history = exerciseDao.getAllExerciseHistory().first()
        val routines = routineDao.getRoutines().first()
        return BackupPayload(
            exercises = exercises.map { ExerciseBackup.fromEntity(it.exercise) },
            personalRecords = exercises.mapNotNull { it.record?.let { pr -> PersonalRecordBackup.fromEntity(pr) } },
            exerciseHistory = history.map { ExerciseHistoryBackup(it.exerciseId, it.date.toString()) },
            routines = routines.map { RoutineBackup.fromEntity(it.routine) },
            routineExercises = routines.flatMap { it.exercises.map { ex -> RoutineExerciseBackup.fromEntity(ex) } },
            sessions = sessions.map { SessionBackup.fromEntity(it.session) },
            sets = sessions.flatMap { it.sets.map { set -> WorkoutSetBackup.fromEntity(set) } },
            dailyLogs = logs.map { DailyLogBackup.fromEntity(it) }
        )
    }

    private fun mapToExerciseOverview(
        exercises: List<com.example.gymapplktrack.data.local.entity.ExerciseWithRecord>,
        usageCounts: List<ExerciseUsageCount>
    ): List<ExerciseOverview> {
        val usageMap = usageCounts.associate { it.exerciseId to it.usageCount }
        return exercises.map { item ->
            ExerciseOverview(
                id = item.exercise.id,
                name = item.exercise.name,
                imageUri = item.exercise.imageUri,
                category = item.exercise.category,
                notes = item.exercise.notes,
                personalRecord = item.record?.let {
                    PersonalRecord(
                        bestWeightKg = it.bestWeightKg,
                        bestReps = it.bestReps,
                        firstAchievedDate = it.firstAchievedDate
                    )
                },
                usageCount = usageMap[item.exercise.id] ?: 0
            )
        }
    }

    private fun WorkoutSetWithSession.toDomain(): ExerciseSetHistory = ExerciseSetHistory(
        sessionId = sessionId,
        date = date,
        startTime = startTime,
        endTime = endTime,
        setIndex = setIndex,
        weightKg = weightKg,
        reps = reps,
        notes = sessionNotes
    )

    private fun PersonalRecordEntity.toCandidate(): PersonalRecordCalculator.Candidate =
        PersonalRecordCalculator.Candidate(bestWeightKg, bestReps, firstAchievedDate)
}

private data class BackupPayload(
    val exercises: List<ExerciseBackup>,
    val personalRecords: List<PersonalRecordBackup>,
    val exerciseHistory: List<ExerciseHistoryBackup>,
    val routines: List<RoutineBackup>,
    val routineExercises: List<RoutineExerciseBackup>,
    val sessions: List<SessionBackup>,
    val sets: List<WorkoutSetBackup>,
    val dailyLogs: List<DailyLogBackup>
)

private data class ExerciseBackup(
    val id: Long,
    val name: String,
    val imageUri: String?,
    val category: String?,
    val notes: String?
) {
    fun toEntity() = ExerciseEntity(id, name, imageUri, category, notes)

    companion object {
        fun fromEntity(entity: ExerciseEntity) = ExerciseBackup(
            id = entity.id,
            name = entity.name,
            imageUri = entity.imageUri,
            category = entity.category,
            notes = entity.notes
        )
    }
}

private data class PersonalRecordBackup(
    val exerciseId: Long,
    val bestWeightKg: Float,
    val bestReps: Int,
    val firstAchievedDate: String
) {
    fun toEntity() = PersonalRecordEntity(
        exerciseId = exerciseId,
        bestWeightKg = bestWeightKg,
        bestReps = bestReps,
        firstAchievedDate = LocalDate.parse(firstAchievedDate)
    )

    companion object {
        fun fromEntity(entity: PersonalRecordEntity) = PersonalRecordBackup(
            exerciseId = entity.exerciseId,
            bestWeightKg = entity.bestWeightKg,
            bestReps = entity.bestReps,
            firstAchievedDate = entity.firstAchievedDate.toString()
        )
    }
}

private data class ExerciseHistoryBackup(
    val exerciseId: Long,
    val date: String
) {
    fun toEntity() = ExerciseHistoryEntity(exerciseId = exerciseId, date = LocalDate.parse(date))
}

private data class RoutineBackup(
    val id: Long,
    val name: String,
    val createdAt: String
) {
    fun toEntity() = RoutineEntity(id = id, name = name, createdAt = LocalDateTime.parse(createdAt))

    companion object {
        fun fromEntity(entity: RoutineEntity) = RoutineBackup(
            id = entity.id,
            name = entity.name,
            createdAt = entity.createdAt.toString()
        )
    }
}

private data class RoutineExerciseBackup(
    val id: Long,
    val routineId: Long,
    val exerciseId: Long,
    val order: Int
) {
    fun toEntity() = RoutineExerciseEntity(id = id, routineId = routineId, exerciseId = exerciseId, order = order)

    companion object {
        fun fromEntity(entity: RoutineExerciseEntity) = RoutineExerciseBackup(
            id = entity.id,
            routineId = entity.routineId,
            exerciseId = entity.exerciseId,
            order = entity.order
        )
    }
}

private data class SessionBackup(
    val id: Long,
    val date: String,
    val startTime: String,
    val endTime: String,
    val notes: String?
) {
    fun toEntity() = WorkoutSessionEntity(
        id = id,
        date = LocalDate.parse(date),
        startTime = LocalTime.parse(startTime),
        endTime = LocalTime.parse(endTime),
        notes = notes
    )

    companion object {
        fun fromEntity(entity: WorkoutSessionEntity) = SessionBackup(
            id = entity.id,
            date = entity.date.toString(),
            startTime = entity.startTime.toString(),
            endTime = entity.endTime.toString(),
            notes = entity.notes
        )
    }
}

private data class WorkoutSetBackup(
    val id: Long,
    val sessionId: Long,
    val exerciseId: Long,
    val setIndex: Int,
    val weightKg: Float,
    val reps: Int
) {
    fun toEntity() = WorkoutSetEntity(
        id = id,
        sessionId = sessionId,
        exerciseId = exerciseId,
        setIndex = setIndex,
        weightKg = weightKg,
        reps = reps
    )

    companion object {
        fun fromEntity(entity: WorkoutSetEntity) = WorkoutSetBackup(
            id = entity.id,
            sessionId = entity.sessionId,
            exerciseId = entity.exerciseId,
            setIndex = entity.setIndex,
            weightKg = entity.weightKg,
            reps = entity.reps
        )
    }
}

private data class DailyLogBackup(
    val date: String,
    val didWorkout: Boolean,
    val tookCreatine: Boolean
) {
    fun toEntity() = DailyLogEntity(
        date = LocalDate.parse(date),
        didWorkout = didWorkout,
        tookCreatine = tookCreatine
    )

    companion object {
        fun fromEntity(entity: DailyLogEntity) = DailyLogBackup(
            date = entity.date.toString(),
            didWorkout = entity.didWorkout,
            tookCreatine = entity.tookCreatine
        )
    }
}
