package com.example.altong_v2.data.local

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    // Date -> Long 변환 (저장)
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    // Long -> Date 변환 (조회)
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}