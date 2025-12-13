package com.example.altong_v2.data.repository

import android.util.Log
import com.example.altong_v2.data.local.dao.FavoriteMedicineDao
import com.example.altong_v2.data.local.entity.FavoriteMedicineEntity
import com.example.altong_v2.data.model.Medicine
import kotlinx.coroutines.flow.Flow

/**
 * 찜 기능 전용 Repository
 * Room DB를 사용한 로컬 저장소 관리
 */
class FavoriteMedicineRepository(
    private val favoriteMedicineDao: FavoriteMedicineDao
) {
    private val TAG = "FavoriteMedicineRepository"

    // ========== 찜 기능 ==========

    /**
     * 모든 찜한 약품 조회
     */
    val allFavorites: Flow<List<FavoriteMedicineEntity>> =
        favoriteMedicineDao.getAllFavorites()

    /**
     * 타입별 찜 목록 조회 (일반의약품 or 전문의약품)
     */
    fun getFavoritesByType(type: String): Flow<List<FavoriteMedicineEntity>> {
        return favoriteMedicineDao.getFavoritesByType(type)
    }

    /**
     * 찜 추가
     */
    suspend fun addFavorite(favorite: FavoriteMedicineEntity) {
        val existing = favoriteMedicineDao.getFavoriteByMedicineId(favorite.medicineId)

        if (existing != null) {
            // 이미 있으면 찜만 활성화
            favoriteMedicineDao.refavorite(favorite.medicineId)
        } else {
            // 없으면 새로 추가
            favoriteMedicineDao.insert(favorite)
        }
    }

    /**
     * 찜 삭제
     */
    suspend fun removeFavorite(favorite: FavoriteMedicineEntity) {
        favoriteMedicineDao.delete(favorite)
    }

    /**
     * 약품 ID로 찜 해제 (메모는 유지)
     */
    suspend fun removeFavoriteById(medicineId: String) {
        val favorite = favoriteMedicineDao.getFavoriteByMedicineId(medicineId)

        if (favorite != null) {
            if (favorite.memo.isNullOrBlank()) {
                // 메모 없으면 완전 삭제
                favoriteMedicineDao.deleteByMedicineId(medicineId)
            } else {
                // 메모 있으면 찜만 해제
                favoriteMedicineDao.unfavorite(medicineId)
            }
        }
    }

    /**
     * 찜 여부 확인
     */
    suspend fun isFavorite(medicineId: String): Boolean {
        return favoriteMedicineDao.isFavorite(medicineId)
    }

    /**
     * 찜 메모 수정
     */
    suspend fun updateFavorite(favorite: FavoriteMedicineEntity) {
        favoriteMedicineDao.update(favorite)
    }

    /**
     * 찜 개수
     */
    suspend fun getFavoriteCount(): Int {
        return favoriteMedicineDao.getCount()
    }

    // ========== 메모 기능 ==========

    /**
     * 메모 저장/수정 (찜 자동 추가)
     */
    suspend fun saveMemo(medicine: Medicine, memo: String) {
        val favorite = favoriteMedicineDao.getFavoriteByMedicineId(medicine.medicine_id)

        if (favorite != null) {
            // 이미 있으면 메모만 업데이트
            favoriteMedicineDao.updateMemo(medicine.medicine_id, memo.ifBlank { null })

            // 메모 추가 시 찜도 자동 활성화
            if (memo.isNotBlank()) {
                favoriteMedicineDao.refavorite(medicine.medicine_id)
            }
        } else {
            // 없으면 새로 추가 (자동 찜)
            val newFavorite = FavoriteMedicineEntity(
                medicineId = medicine.medicine_id,
                medicineName = medicine.medicine_name,
                manufacturer = medicine.manufacturer,
                medicineType = "general",
                imageUrl = medicine.image_url ?: "",
                isFavorite = true,
                memo = memo.ifBlank { null }
            )
            favoriteMedicineDao.insert(newFavorite)
        }
    }

    /**
     * 메모 조회
     */
    suspend fun getMemo(medicineId: String): String? {
        return favoriteMedicineDao.getMemo(medicineId)
    }
}