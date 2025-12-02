package com.example.altong_v2.data.repository

import com.example.altong_v2.data.local.dao.FavoriteMedicineDao
import com.example.altong_v2.data.local.entity.FavoriteMedicineEntity
import kotlinx.coroutines.flow.Flow


/* * 약 검색 Repository
 * Firebase에서 약 정보 조회 + 찜 기능 관리
 * TODO (민주가): Firebase Firestore 연동 추가*/

class MedicineRepository(
    private val favoriteMedicineDao: FavoriteMedicineDao
) {

    // ========== 찜 기능 (Room DB 사용) ==========
    // 모든 찜한 약 조회
    val allFavorites: Flow<List<FavoriteMedicineEntity>> =
        favoriteMedicineDao.getAllFavorites()
    // 타입별 찜 목록 조회
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
    // 약 ID로 찜 삭제
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

    // ========== Firebase 약 검색 (민주가 구현) ==========
    // TODO (팀원): Firebase에서 약 검색
    // suspend fun searchMedicines(query: String): List<Medicine> {
    //     // Firebase Firestore 쿼리
    // }

    // TODO (팀원): 카테고리별 약 조회
    // suspend fun getMedicinesByCategory(category: String): List<Medicine> {
    //     // Firebase Firestore 쿼리
    // }

    // TODO (팀원): 약 상세 정보 조회
    // suspend fun getMedicineById(medicineId: String): Medicine? {
    //     // Firebase Firestore 쿼리
    // }
}