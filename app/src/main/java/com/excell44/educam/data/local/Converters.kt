package com.excell44.educam.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(value: String): List<String> = 
        if (value.isBlank()) emptyList() else value.split(",").map { it.trim() }
    
    @TypeConverter
    fun toStringList(list: List<String>): String = list.joinToString(",")
}
