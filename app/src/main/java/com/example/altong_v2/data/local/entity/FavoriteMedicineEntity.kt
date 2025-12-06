package com.example.altong_v2.data.local.entity


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


/* * 찜한 약품 테이블
 * 일반의약품 또는 전문의약품을 찜한 목록 저장 */

@Entity(
    tableName = "favorite_medicines",
    indices = [Index(value = ["medicine_id"], unique = true)]  // 중복 찜 방지
)
data class FavoriteMedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 약품 정보
    @ColumnInfo(name = "medicine_id")
    val medicineId: String,          // Firebase의 약품 ID

    @ColumnInfo(name = "medicine_name")
    val medicineName: String,        // 약품명

    val manufacturer: String,        // 제조사

    @ColumnInfo(name = "medicine_type")
    val medicineType: String,        // 약품 유형 ("otc" or "prescription")

    @ColumnInfo(name = "image_url")
    val imageUrl: String = "",       // 이미지 URL

    // 개인 메모
    val memo: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)