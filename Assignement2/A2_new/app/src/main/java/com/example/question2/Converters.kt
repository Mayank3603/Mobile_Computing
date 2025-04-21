package com.example.question2

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime

object Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toLocalDate(dateStr: String): LocalDate = LocalDate.parse(dateStr)

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime): String = dateTime.toString()

    @TypeConverter
    fun toLocalDateTime(dateTimeStr: String): LocalDateTime = LocalDateTime.parse(dateTimeStr)
}
