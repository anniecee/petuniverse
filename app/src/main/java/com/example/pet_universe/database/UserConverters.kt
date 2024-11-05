package com.example.pet_universe.database

import androidx.room.TypeConverter
import androidx.room.TypeConverters

class UserConverters {
    @TypeConverter
    fun fromList(list: List<Long>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toList(data: String): List<Long> {
        return data.split(",").map { it.toLong() }
    }
}
