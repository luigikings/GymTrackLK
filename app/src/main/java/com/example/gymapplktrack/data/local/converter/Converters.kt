package com.example.gymapplktrack.data.local.converter

import androidx.room.TypeConverter
import com.example.gymapplktrack.data.local.entity.ActiveWorkoutPayload
import com.google.gson.Gson
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun fromLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @TypeConverter
    fun localTimeToString(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    fun fromLocalDateTime(value: String?): LocalDateTime? = value?.let(LocalDateTime::parse)

    @TypeConverter
    fun localDateTimeToString(dateTime: LocalDateTime?): String? = dateTime?.toString()

    @TypeConverter
    fun fromActiveWorkoutPayload(value: String?): ActiveWorkoutPayload? =
        value?.let { gson.fromJson(it, ActiveWorkoutPayload::class.java) }

    @TypeConverter
    fun activeWorkoutPayloadToString(payload: ActiveWorkoutPayload?): String? =
        payload?.let { gson.toJson(it) }
}
