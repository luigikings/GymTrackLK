package com.example.gymapplktrack.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ExerciseWithRecord(
    @Embedded val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId"
    )
    val record: PersonalRecordEntity?
)

data class RoutineWithExercises(
    @Embedded val routine: RoutineEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val exercises: List<RoutineExerciseEntity>
)

data class SessionWithSets(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val sets: List<WorkoutSetEntity>
)
