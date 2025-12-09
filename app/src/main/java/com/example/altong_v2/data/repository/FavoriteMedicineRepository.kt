package com.example.altong_v2.data.repository

import com.example.altong_v2.data.local.dao.FavoriteMedicineDao
import com.example.altong_v2.data.local.entity.FavoriteMedicineEntity
import kotlinx.coroutines.flow.Flow

/* * 찜 기능 전용 Repository
 * Room DB를 사용한 로컬 저장소 관리 */

class FavoriteMedicineRepository(
    private val favoriteMedicineDao: FavoriteMedicineDao
) {
    // 모든 찜한 약 조회
    val allFavorites: Flow<List<FavoriteMedicineEntity>> =
        favoriteMedicineDao.getAllFavorites()
    // 타입별 찜목록 조회(일반 or 전문)
    fun getFavoritesByType(type: String): Flow<List<FavoriteMedicineEntity>> {
        return favoriteMedicineDao.getFavoritesByType(type)
    }
    // 찜 추가
    suspend fun addFavorite(favorite: FavoriteMedicineEntity): Long {
        return favoriteMedicineDao.insert(favorite)
    }
    // 찜 삭제
    suspend fun removeFavorite(favorite: FavoriteMedicineEntity) {
        favoriteMedicineDao.delete(favorite)
    }
    // 약품아이디로 찜 삭제
    suspend fun removeFavoriteById(medicineId: String) {
        favoriteMedicineDao.deleteByMedicineId(medicineId)
    }
    // 찜 여부 확인
    suspend fun isFavorite(medicineId: String): Boolean {
        return favoriteMedicineDao.isFavorite(medicineId)
    }
    // 찜 메모 수정
    suspend fun updateFavorite(favorite: FavoriteMedicineEntity) {
        favoriteMedicineDao.update(favorite)
    }
    // 찜 개수
    suspend fun getFavoriteCount(): Int {
        return favoriteMedicineDao.getCount()
    }
}