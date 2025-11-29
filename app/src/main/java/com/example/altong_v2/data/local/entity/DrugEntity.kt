package com.example.altong_v2.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


/*
 * 약품 테이블
 * 처방전에 포함된 약품 정보를 저장
 * 처방전 삭제 시 해당 약품도 자동 삭제 (CASCADE) */


@Entity(
    tableName = "drugs",
    foreignKeys = [
        ForeignKey(
            entity = PrescriptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["prescription_id"],
            onDelete = ForeignKey.CASCADE  // 처방전 삭제 시 약품도 삭제됨.
        )
    ],
    indices = [Index(value = ["prescription_id"])]
)
data class DrugEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 처방전 연결
    @ColumnInfo(name = "prescription_id")
    val prescriptionId: Long,

    // 약품 기본 정보
    val name: String,                // 약품명
    val dosage: String,              // 1회 복용량 (예: "1정")
    val frequency: String,           // 1일 복용 횟수 (예: "3회")
    val days: Int,                   // 총 처방 일수

    // 복용 정보
    val timing: String?,             // 복용 시점 (예: "식후 30분", 선택)
    val memo: String?,               // 개인 메모 (선택)

    // 알림 시간대 (쉼표로 구분된 문자열)
    // 예: "아침,저녁" 또는 "아침,점심,저녁,취침 전"
    @ColumnInfo(name = "time_slots")
    val timeSlots: String
)




