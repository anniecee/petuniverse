package com.example.pet_universe.database

import androidx.room.TypeConverter
import java.util.Calendar

class Converters {
    @TypeConverter
    fun fromList(list: List<Long>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toList(data: String): List<Long> {
        return data.split(",").map { it.toLong() }
    }

    @TypeConverter
    fun fromCalendar(value: Long): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = value
        return calendar
    }

    @TypeConverter
    fun toCalendar(calendar: Calendar): Long {
        return calendar.timeInMillis
    }

    @TypeConverter
    fun fromMap(map: Map<String, Double>): String {
        return map.entries.joinToString(",") { "${it.key}:${it.value}" }
    }

    @TypeConverter
    fun toMap(data: String): Map<String, Double> {
        return data.split(",").map {
            val (key, value) = it.split(":")
            key to value.toDouble()
        }.toMap()
    }
}
