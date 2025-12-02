package com.example.altong_v2.data.local.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.altong_v2.data.local.entity.FavoriteMedicineEntity
import kotlinx.coroutines.flow.Flow


/* * 찜한 약 DAO
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

    // 타입별 찜 목록 조회 (일반의약 or 전문의약)
    @Query("SELECT * FROM favorite_medicines WHERE type = :type ORDER BY created_at DESC")
    fun getFavoritesByType(type: String): Flow<List<FavoriteMedicineEntity>>

    // 약 ID로 찜 조회
    @Query("SELECT * FROM favorite_medicines WHERE medicine_id = :medicineId")
    suspend fun getFavoriteByMedicineId(medicineId: String): FavoriteMedicineEntity?

    // 찜 여부 확인
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_medicines WHERE medicine_id = :medicineId)")
    suspend fun isFavorite(medicineId: String): Boolean

    // 약 ID로 찜 삭제
    @Query("DELETE FROM favorite_medicines WHERE medicine_id = :medicineId")
    suspend fun deleteByMedicineId(medicineId: String)

    // 모든 찜 삭제
    @Query("DELETE FROM favorite_medicines")
    suspend fun deleteAll()

    // 찜 개수 조회
    @Query("SELECT COUNT(*) FROM favorite_medicines")
    suspend fun getCount(): Int
}




