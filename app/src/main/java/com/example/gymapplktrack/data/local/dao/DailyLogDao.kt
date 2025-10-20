package com.example.gymapplktrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gymapplktrack.data.local.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getLogs(): Flow<List<DailyLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: DailyLogEntity)

    @Query("UPDATE daily_logs SET tookCreatine = :tookCreatine WHERE date = :date")
    suspend fun updateCreatine(date: java.time.LocalDate, tookCreatine: Boolean)

    @Query("UPDATE daily_logs SET didWorkout = :didWorkout WHERE date = :date")
    suspend fun updateDidWorkout(date: java.time.LocalDate, didWorkout: Boolean)
}
