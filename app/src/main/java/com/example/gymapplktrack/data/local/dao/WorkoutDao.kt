package com.example.gymapplktrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.gymapplktrack.data.local.entity.SessionWithSets
import com.example.gymapplktrack.data.local.entity.WorkoutSessionEntity
import com.example.gymapplktrack.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY date DESC, startTime DESC")
    fun getSessions(): Flow<List<SessionWithSets>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<WorkoutSetEntity>)

    @Query(
        "SELECT ws.id as sessionId, ws.date as date, ws.startTime as startTime, ws.endTime as endTime, ws.notes as sessionNotes, " +
            "s.id as setId, s.exerciseId as exerciseId, s.setIndex as setIndex, s.weightKg as weightKg, s.reps as reps " +
            "FROM workout_sets s INNER JOIN workout_sessions ws ON ws.id = s.sessionId " +
            "WHERE s.exerciseId = :exerciseId ORDER BY ws.date DESC, ws.startTime DESC, s.setIndex ASC"
    )
    fun getSetsForExercise(exerciseId: Long): Flow<List<WorkoutSetWithSession>>
}

data class WorkoutSetWithSession(
    val sessionId: Long,
    val date: java.time.LocalDate,
    val startTime: java.time.LocalTime,
    val endTime: java.time.LocalTime,
    val sessionNotes: String?,
    val setId: Long,
    val exerciseId: Long,
    val setIndex: Int,
    val weightKg: Float,
    val reps: Int
)
