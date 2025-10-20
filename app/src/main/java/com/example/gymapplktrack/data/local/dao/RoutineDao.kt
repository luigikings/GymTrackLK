package com.example.gymapplktrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.gymapplktrack.data.local.entity.RoutineEntity
import com.example.gymapplktrack.data.local.entity.RoutineExerciseEntity
import com.example.gymapplktrack.data.local.entity.RoutineWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Transaction
    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun getRoutines(): Flow<List<RoutineWithExercises>>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id")
    fun getRoutine(id: Long): Flow<RoutineWithExercises?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(exercises: List<RoutineExerciseEntity>)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteRoutineExercises(routineId: Long)

    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: Long)
}
