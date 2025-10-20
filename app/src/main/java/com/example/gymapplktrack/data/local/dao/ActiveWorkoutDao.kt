package com.example.gymapplktrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gymapplktrack.data.local.entity.ActiveWorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveWorkoutDao {
    @Query("SELECT * FROM active_workout LIMIT 1")
    fun observeActiveWorkout(): Flow<ActiveWorkoutEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActiveWorkout(entity: ActiveWorkoutEntity)

    @Query("DELETE FROM active_workout")
    suspend fun clear()
}
