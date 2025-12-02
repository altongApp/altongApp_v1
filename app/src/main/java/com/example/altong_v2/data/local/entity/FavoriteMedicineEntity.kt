package com.example.altong_v2.data.local.entity


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


/* * 찜한 약 테이블
 * 일반의약 또는 전문의약을 찜한 목록 저장 */

@Entity(
    tableName = "favorite_medicines",
    indices = [Index(value = ["medicine_id"], unique = true)]  // 중복 찜 방지
)
data class FavoriteMedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 약 정보
    @ColumnInfo(name = "medicine_id")
    val medicineId: String,          // Firebase의 약 ID

    val name: String,                // 약명
    val company: String,             // 제조사
    val type: String,                // 약 유형(일반의약 or 전문의약)

    // 개인 메모
    val memo: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)




