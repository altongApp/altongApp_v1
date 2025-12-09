package com.example.altong_v2.data.local.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.altong_v2.data.local.entity.FavoriteMedicineEntity
import kotlinx.coroutines.flow.Flow


/* * 찜한 약품 DAO
 * 찜 목록 테이블에 대한 CRUD 작업 정의*/

@Dao
interface FavoriteMedicineDao {

    // 찜 추가 (중복 시 교체)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favoriteMedicine: FavoriteMedicineEntity): Long

    // 찜 수정 (메모 수정용)
    @Update
    suspend fun update(favoriteMedicine: FavoriteMedicineEntity)

    // 찜 삭제
    @Delete
    suspend fun delete(favoriteMedicine: FavoriteMedicineEntity)

    // 모든 찜 목록 조회 (최신순)
    @Query("SELECT * FROM favorite_medicines ORDER BY created_at DESC")
    fun getAllFavorites(): Flow<List<FavoriteMedicineEntity>>

    // 약품 ID로 찜 조회
    @Query("SELECT * FROM favorite_medicines WHERE medicine_id = :medicineId")
    suspend fun getFavoriteByMedicineId(medicineId: String): FavoriteMedicineEntity?

    // 약품 ID로 찜 삭제
    @Query("DELETE FROM favorite_medicines WHERE medicine_id = :medicineId")
    suspend fun deleteByMedicineId(medicineId: String)

    // 모든 찜 삭제
    @Query("DELETE FROM favorite_medicines")
    suspend fun deleteAll()

    // 찜 개수 조회
    @Query("SELECT COUNT(*) FROM favorite_medicines")
    suspend fun getCount(): Int

    // FavoriteMedicineDao.kt에 추가할 함수

    /**
     * 메모 업데이트 (메모 수정)
     */
    @Query("UPDATE favorite_medicines SET memo = :memo WHERE medicine_id = :medicineId")
    suspend fun updateMemo(medicineId: String, memo: String?)

    /**
     * 메모 조회
     */
    @Query("SELECT memo FROM favorite_medicines WHERE medicine_id = :medicineId")
    suspend fun getMemo(medicineId: String): String?

    // FavoriteMedicineDao.kt 수정/추가할 함수들

    /**
     * 타입별 찜 목록 조회 (isFavorite = true인 것만)
     */
    @Query("SELECT * FROM favorite_medicines WHERE medicine_type = :type AND is_favorite = 1 ORDER BY created_at DESC")
    fun getFavoritesByType(type: String): Flow<List<FavoriteMedicineEntity>>

    /**
     * ⭐ 찜 여부 확인 (isFavorite = true)
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_medicines WHERE medicine_id = :medicineId AND is_favorite = 1)")
    suspend fun isFavorite(medicineId: String): Boolean

    /**
     * ⭐ 찜 상태 업데이트 (찜 해제)
     */
    @Query("UPDATE favorite_medicines SET is_favorite = 0 WHERE medicine_id = :medicineId")
    suspend fun unfavorite(medicineId: String)

    /**
     * ⭐ 찜 상태 업데이트 (찜 추가)
     */
    @Query("UPDATE favorite_medicines SET is_favorite = 1 WHERE medicine_id = :medicineId")
    suspend fun refavorite(medicineId: String)

    /**
     * ⭐ 메모 없고 찜도 해제된 항목 삭제 (청소용)
     */
    @Query("DELETE FROM favorite_medicines WHERE is_favorite = 0 AND (memo IS NULL OR memo = '')")
    suspend fun cleanupUnused()
}