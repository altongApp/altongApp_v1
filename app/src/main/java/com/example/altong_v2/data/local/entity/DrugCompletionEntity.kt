package com.example.altong_v2.data.local.entity


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


/* 약 복용 완료 기록 테이블
 * 캘린더에서 체크박스 체크 시 기록*/

@Entity(
    tableName = "drug_completions",
    foreignKeys = [
        ForeignKey(
            entity = DrugEntity::class,
            parentColumns = ["id"],
            childColumns = ["drug_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["drug_id", "date"], unique = true)]  // 같은 날짜 중복 방지
)
data class DrugCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 약 연결
    @ColumnInfo(name = "drug_id")
    val drugId: Long,

    // 복용 정보
    val date: String,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,        // 복용 완료 여부
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null    // 복용 완료 시각 (timestamp)
)