package com.example.altong_v2.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


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
    val medicineType: String,        //  "general" or "prescription"

    @ColumnInfo(name = "image_url")
    val imageUrl: String = "",       // 이미지 URL

    //  찜 상태 플래그 (새로 추가!)
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = true,  // true=찜, false=찜 해제 (메모만 남음)

    // 개인 메모
    val memo: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)