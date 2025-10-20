package com.example.gymapplktrack.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.gymapplktrack.data.local.entity.ExerciseEntity
import com.example.gymapplktrack.data.local.entity.ExerciseHistoryEntity
import com.example.gymapplktrack.data.local.entity.ExerciseWithRecord
import com.example.gymapplktrack.data.local.entity.PersonalRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Transaction
    @Query("SELECT * FROM exercises")
    fun getExercisesWithRecord(): Flow<List<ExerciseWithRecord>>

    @Transaction
    @Query("SELECT * FROM exercises WHERE id = :id")
    fun getExerciseWithRecord(id: Long): Flow<ExerciseWithRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("SELECT DISTINCT category FROM exercises WHERE category IS NOT NULL AND category != '' ORDER BY category")
    fun getCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPersonalRecord(record: PersonalRecordEntity)

    @Query("DELETE FROM personal_records WHERE exerciseId = :exerciseId")
    suspend fun deletePersonalRecord(exerciseId: Long)

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId")
    fun getPersonalRecord(exerciseId: Long): Flow<PersonalRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExerciseHistory(entries: List<ExerciseHistoryEntity>)

    @Query("SELECT * FROM exercise_history WHERE exerciseId = :exerciseId")
    fun getExerciseHistory(exerciseId: Long): Flow<List<ExerciseHistoryEntity>>

    @Query("SELECT * FROM exercise_history")
    fun getAllExerciseHistory(): Flow<List<ExerciseHistoryEntity>>

    @Query(
        "SELECT exerciseId, COUNT(*) as usageCount FROM workout_sets GROUP BY exerciseId"
    )
    fun getExerciseUsageCounts(): Flow<List<ExerciseUsageCount>>
}

data class ExerciseUsageCount(
    val exerciseId: Long,
    val usageCount: Int
)
