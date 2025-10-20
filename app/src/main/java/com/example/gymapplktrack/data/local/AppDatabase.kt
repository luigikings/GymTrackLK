package com.example.gymapplktrack.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gymapplktrack.data.local.converter.Converters
import com.example.gymapplktrack.data.local.dao.ActiveWorkoutDao
import com.example.gymapplktrack.data.local.dao.DailyLogDao
import com.example.gymapplktrack.data.local.dao.ExerciseDao
import com.example.gymapplktrack.data.local.dao.RoutineDao
import com.example.gymapplktrack.data.local.dao.WorkoutDao
import com.example.gymapplktrack.data.local.entity.ActiveWorkoutEntity
import com.example.gymapplktrack.data.local.entity.DailyLogEntity
import com.example.gymapplktrack.data.local.entity.ExerciseEntity
import com.example.gymapplktrack.data.local.entity.ExerciseHistoryEntity
import com.example.gymapplktrack.data.local.entity.PersonalRecordEntity
import com.example.gymapplktrack.data.local.entity.RoutineEntity
import com.example.gymapplktrack.data.local.entity.RoutineExerciseEntity
import com.example.gymapplktrack.data.local.entity.WorkoutSessionEntity
import com.example.gymapplktrack.data.local.entity.WorkoutSetEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutSetEntity::class,
        PersonalRecordEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        DailyLogEntity::class,
        ExerciseHistoryEntity::class,
        ActiveWorkoutEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun routineDao(): RoutineDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun activeWorkoutDao(): ActiveWorkoutDao

    companion object {
        fun build(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "gymtracker.db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
