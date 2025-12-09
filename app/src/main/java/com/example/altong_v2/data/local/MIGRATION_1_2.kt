package com.example.altong_v2.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // drugs 테이블에 image_url 컬럼추가
        database.execSQL("ALTER TABLE drugs ADD COLUMN image_url TEXT")
    }
}