package com.example.altong_v2.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


/* 처방전 테이블
병원에서 받은 처방전 정보를 저장 */

@Entity(
    tableName = "prescriptions",
    indices = [Index(value = ["diagnosis"])]
)
data class PrescriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 처방 기본 정보
    val date: String,                // 처방일 (YYYY-MM-DD)
    val hospital: String?,           // 병원명 (선택)
    val department: String?,         // 진료과 (선택)
    val diagnosis: String,           // 진단명 (필수)
    val pharmacy: String?,           // 처방 약국 (선택)

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)